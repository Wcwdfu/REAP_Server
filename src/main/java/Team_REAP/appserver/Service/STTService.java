package Team_REAP.appserver.Service;

import Team_REAP.appserver.util.MetadataUtils;


import lombok.extern.slf4j.Slf4j;
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class STTService {
    //@Value("${naver.cloud.invoke.url}")
    @Value("${naver.cloud.invoke.url}")
    private String apiUrl;

    @Value("${naver.cloud.secret.key}")
    private String secretKey;

    private final UserService userService;
    
    private final MetadataUtils metadataUtils;

    public ResponseEntity<String> audioToText(MultipartFile media, String date, String language, String completion, String callback, boolean wordAlignment, boolean fullText, boolean resultToObs, boolean noiseFiltering) {

        File tempFile = null;
        IsoFile isoFile = null;
        try {

            /*
             * 메타 데이터를 통해서 음성 생성 시간 받아오기
             * */
            // MultipartFile을 임시 파일로 저장
            tempFile = metadataUtils.saveMultipleFileToTmpFile(media);
            
            // MP4 파일 메타데이터 읽기
            LocalDateTime creationDateTime = metadataUtils.readCreationTimeFromMp4(tempFile);
            
            // 시간대를 변환 (예: 한국 시간대로 변환)
            LocalDateTime creationDateTimeKST = metadataUtils.convertToKST(creationDateTime);

            // 날짜와 시간 부분만 추출
            //String creationDateKST = metadataUtils.formatDateTime(creationDateTime, "yyyy-MM-dd");
            //String creationTimeKST = metadataUtils.formatDateTime(creationDateTime, "HH:mm:ss");

            //log.info("Creation Date (KST): " + creationDateKST);
            //log.info("Creation Time (KST): " + creationTimeKST);

            /*
             * NaverCloud에 STT 요청하기
             * */
            ResponseEntity<String> responseEntity = requestSttToNaverCloud(tempFile, language, completion, callback, wordAlignment, fullText, resultToObs, noiseFiltering);


            /*
            * 대화 스크립트 제작
            * */
            String responseBody = responseEntity.getBody();
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray segments = jsonObject.getJSONArray("segments");

            // 대화 스크립트
            StringBuilder recordInfo = new StringBuilder();

            // 누적 시간을 저장할 변수
            Duration totalDuration = Duration.ZERO;

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            // GPT에게 요청해서 답변 받아오기 + 스크립트 만들기
            log.info("대화 스크립트 만들기");
            for (int i = 0; i < segments.length(); i++) {
                JSONObject segment = segments.getJSONObject(i);
                int start = segment.getInt("start");
                int end = segment.getInt("end");
                String text = segment.getString("text");
                String speakerName = segment.getJSONObject("speaker").getString("name");

                // start와 end의 차이를 계산하여 누적 시간에 더하기
                int durationMillis = end - start;
                totalDuration = totalDuration.plusMillis(durationMillis);

                // 누적 시간을 creationDateTimeKST에 더하기
                LocalDateTime adjustedDateTimeKST = creationDateTimeKST.plus(totalDuration);

                String adjustedDateKST = adjustedDateTimeKST.format(dateFormatter);
                String adjustedTimeKST = adjustedDateTimeKST.format(timeFormatter);

                recordInfo.append(adjustedDateKST).append(" ");
                recordInfo.append(adjustedTimeKST).append(" ");
                recordInfo.append(speakerName).append(" ");
                recordInfo.append(text).append("\n");

                log.info("userService 이용");
                userService.create(speakerName, adjustedDateKST, adjustedTimeKST, text);
            }

            // 전체 녹음 스크립트
            log.info("{}", recordInfo);

            return responseEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        } finally {
            closeIsoFile(isoFile);
            deleteTmpFile(tempFile);
        }
    }

    private ResponseEntity<String> requestSttToNaverCloud(File tempFile, String language, String completion, String callback, boolean wordAlignment, boolean fullText, boolean resultToObs, boolean noiseFiltering) {
        String url = apiUrl + "/recognizer/upload";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("X-CLOVASPEECH-API-KEY", secretKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        log.info(tempFile.getName());
        body.add("media", new FileSystemResource(tempFile));

        Map<String, Object> params = new HashMap<>();
        params.put("language", language);
        params.put("completion", completion);
        params.put("callback", callback);
        params.put("wordAlignment", wordAlignment);
        params.put("fullText", fullText);
        params.put("resultToObs", resultToObs);
        params.put("noiseFiltering", noiseFiltering);

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

    private static void deleteTmpFile(File tempFile) {
        // IsoFile을 사용한 후 닫아줌, 사용 중이면 파일이 삭제가 안 된다.
        if (tempFile != null && tempFile.exists()) {
            boolean deleted = tempFile.delete();
            if (!deleted) {
                log.error("임시 파일 삭제에 실패했습니다.");
            }
        }
    }

    private static void closeIsoFile(IsoFile isoFile) {
        if (isoFile != null) {
            try {
                isoFile.close();
            } catch (Exception e) {
                log.error("IsoFile을 닫는 중 오류가 발생했습니다: " + e.getMessage());
            }
        }
    }
}
