package Team_REAP.appserver.BH_file.Service;

import Team_REAP.appserver.BH_file.util.HashUtils;
import Team_REAP.appserver.BH_file.util.MetadataUtils;


import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mp4parser.IsoFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class STTService {
    @Value("${naver.cloud.invoke.url}")
    private String apiUrl;

    @Value("${naver.cloud.secret.key}")
    private String secretKey;

    private final UserService userService;
    
    private final MetadataUtils metadataUtils;

    private final S3Service s3Service;

    public ResponseEntity<String> audioToText(MultipartFile media, String userName) throws IOException {

        File tempFile = null;
        IsoFile isoFile = null;
        try {

            // 임시 파일 생성
            tempFile = metadataUtils.saveMultipleFileToTmpFile(media);
            ResponseEntity<String> responseEntity = requestSttToNaverCloud(tempFile);


            //메타 데이터를 통해서 음성 생성 시간 받아오기
            // MP4 파일 메타데이터 읽기
            LocalDateTime creationDateTime = metadataUtils.readCreationTimeFromMp4(tempFile);
            LocalDateTime creationDateTimeKST = metadataUtils.convertToKST(creationDateTime);

            // 날짜와 시간 부분만 추출
            String creationDateKST = metadataUtils.formatDateTime(creationDateTime, "yyyy-MM-dd");
            //log.info("Creation Date (KST): " + creationDateKST); - 녹음한 날짜
            //log.info("Creation Time (KST): " + creationTimeKST); - 녹음한 시간

            // 대화 스크립트 제작 ( ReponseEntity<String>, LocalDateTime);
            String script = makeScript(responseEntity, creationDateTimeKST);
            String recordId = HashUtils.generateFileHash(tempFile);
            String objectId = userService.createAll(recordId, creationDateKST, script); // ?

            // S3에 파일 저장
            String fileName = media.getOriginalFilename();
            String audioUrl = s3Service.upload(tempFile, fileName, userName, creationDateKST); // ?


            // 전체 녹음 스크립트
            log.info("{}", script);

            return ResponseEntity.status(HttpStatus.OK).body(objectId);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        } finally {
            log.info(tempFile.getAbsolutePath());
            // isofile 닫는건 늘 false
            // IsoFile을 사용한 후 닫아줌, 사용 중이면 파일이 삭제가 안 된다.
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.error("임시 파일 삭제에 실패했습니다.");
                }
            }
        }
    }

    @NotNull
    private static String makeScript(ResponseEntity<String> responseEntity, LocalDateTime creationDateTimeKST) {
        String responseBody = responseEntity.getBody();
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray segments = jsonObject.getJSONArray("segments");

        // 대화 스크립트
        StringBuilder recordInfo = new StringBuilder();

        // 누적 시간을 저장할 변수
        Duration totalDuration = Duration.ZERO;

        //DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // 스크립트 만들기

        log.info("대화 스크립트 만들기");
        String preSpeakerName = null;
        for (int i = 0; i < segments.length(); i++) {
            JSONObject segment = segments.getJSONObject(i);
            int start = segment.getInt("start");
            int end = segment.getInt("end");
            String text = segment.getString("text");
            String speakerName = segment.getJSONObject("speaker").getString("name");

            // start와 end의 차이를 계산하여 누적 시간에 더하기
            int durationMillis = end - start;
            totalDuration = totalDuration.plusMillis(durationMillis);

            // start를 LocalDateTime으로 변환
            LocalTime startTime = LocalTime.MIDNIGHT.plus(Duration.ofMillis(start));
            String elapseTime = startTime.format(timeFormatter);

            // 누적된 시간을 기준으로 실제 시간 계산
            LocalDateTime adjustedDateTimeKST = creationDateTimeKST.plus(totalDuration);
            String adjustedTimeKST = adjustedDateTimeKST.format(timeFormatter);

            // recordInfo에 시간 정보와 대화 내용 추가
            if(!Objects.equals(preSpeakerName, speakerName)){
                // 화자가 바뀌었으므로 새 줄을 추가
                if (i != 0) {
                    recordInfo.append("\n"); // 첫 번째 항목이 아닌 경우 개행 추가
                }
                recordInfo.append(adjustedTimeKST).append(" ");   // 실제 시간
                recordInfo.append(elapseTime).append(" "); // 누적 시간
                recordInfo.append(speakerName).append(" ");
                recordInfo.append(text).append(" ");
            }else{
                recordInfo.append(text).append(" ");
            }

            preSpeakerName = speakerName;

            log.info("userService 이용");
            // userService.create(speakerName, adjustedDateKST, adjustedTimeKST, text);
        }

        return recordInfo.toString();
    }

    private ResponseEntity<String> requestSttToNaverCloud(File tempFile) {
        String url = apiUrl + "/recognizer/upload";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("X-CLOVASPEECH-API-KEY", secretKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        log.info(tempFile.getName());
        body.add("media", new FileSystemResource(tempFile));

        Map<String, Object> params = new HashMap<>();
        params.put("language", "ko-KR");
        params.put("completion", "sync");
        params.put("callback", null);
        params.put("wordAlignment", true);
        params.put("fullText", true);
        params.put("resultToObs", false);
        params.put("noiseFiltering", true);

        log.info("JSON 객체로 변환");
        // JSON 객체로 변환
        JSONObject jsonParams = new JSONObject(params);

        body.add("params", jsonParams.toString());

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);

        log.info("RestTemplate 시작");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        log.info("RestTemplate exchange 끝");
        return responseEntity;
    }


}
