package Team_REAP.appserver.STT.service;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.DB.mongo.service.ScriptService;
import Team_REAP.appserver.Deprecated.HashUtils;
import Team_REAP.appserver.RAG.RAG.service.ChromaDBService;
import Team_REAP.appserver.STT.dto.AudioUploadDTO;
import Team_REAP.appserver.STT.dto.InvalidFileFormatErrorDTO;
import Team_REAP.appserver.STT.exception.InvalidFileFormatException;
import Team_REAP.appserver.STT.util.MetadataUtils;

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
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class STTService {
    @Value("${naver.cloud.invoke.url}")
    private String apiUrl;

    @Value("${naver.cloud.secret.key}")
    private String secretKey;

    private final ScriptService scriptService; // 변경된 필드명
    private final ChromaDBService chromaDBService; // 추가된 필드

    private final MetadataUtils metadataUtils;
    private final S3Service s3Service;

    public ResponseEntity<Object> audioToText(MultipartFile media, String userName, String topic) throws IOException {

        File tempFile = null;
        IsoFile isoFile = null;
        try {

            // 허용하는 음성 파일 확장자 리스트
            List<String> allowedExtensions = Arrays.asList("wav", "m4a");

            // 파일 이름에서 확장자 추출
            String mediaFileName = Objects.requireNonNull(media.getOriginalFilename());
            String fileExtension = mediaFileName.substring(mediaFileName.lastIndexOf(".") + 1).toLowerCase();

            // 확장자가 허용되지 않는 경우 오류 메시지 반환
            if (!allowedExtensions.contains(fileExtension)) {
                log.info("fileExtension = {}", fileExtension);
                throw new InvalidFileFormatException("invalid file type - please upload audio file(.m4a, .wav)");
            }

            // 임시 파일 복제해서 생성
            tempFile = metadataUtils.saveMultipleFileToTmpFile(media);

            // 네이버 클라우드에 STT 요청
            ResponseEntity<String> responseEntity = requestSttToNaverCloud(tempFile);

            // 메타데이터에서 녹음 시간 받아오기
            LocalDateTime creationDateTime = metadataUtils.readCreationTimeFromMp4(tempFile);
            LocalDateTime creationDateTimeKST = metadataUtils.convertToKST(creationDateTime);

            // 날짜 부분 추출
            String creationDateKST = metadataUtils.formatDateTime(creationDateTime, "yyyy-MM-dd");

            // 스크립트 생성
            String scriptContent = makeScript(responseEntity, creationDateTimeKST);

            // 필요한 정보 생성
            String recordId = HashUtils.generateFileHash(tempFile);
            String fileName = media.getOriginalFilename();
            String uploadedDate = LocalDate.now().toString();
            String uploadedTime = LocalTime.now().toString();

            // Script 객체 생성
            Script script = Script.builder()
                    .recordId(recordId)
                    .userId(userName)
                    .recordName(fileName)
                    .recordedDate(creationDateKST)
                    .uploadedDate(uploadedDate)
                    .uploadedTime(uploadedTime)
                    .topic(topic)
                    .text(scriptContent)
                    .build();

            // MongoDB에 Script 저장
            scriptService.saveScript(script);

            // ChromaDB에 Script 추가
            chromaDBService.addScriptToVectorStore(script);

            // S3에 파일 저장
            String audioS3Url = s3Service.upload(tempFile, fileName, userName, creationDateKST);

            // 전체 녹음 스크립트
            log.info("{}", scriptContent);

            AudioUploadDTO audioUploadDTO = new AudioUploadDTO(fileName, audioS3Url);

            return ResponseEntity.status(HttpStatus.OK).body(audioUploadDTO);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        } catch (InvalidFileFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new InvalidFileFormatErrorDTO(e.getMessage()));
        } finally {
            // 리소스 정리
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

            // start를 LocalTime으로 변환
            LocalTime startTime = LocalTime.MIDNIGHT.plus(Duration.ofMillis(start));
            String elapseTime = startTime.format(timeFormatter);

            // 누적된 시간을 기준으로 실제 시간 계산
            LocalDateTime adjustedDateTimeKST = creationDateTimeKST.plus(totalDuration);
            String adjustedTimeKST = adjustedDateTimeKST.format(timeFormatter);

            // recordInfo에 시간 정보와 대화 내용 추가
            if (!Objects.equals(preSpeakerName, speakerName)) {
                // 화자가 바뀌었으므로 새 줄을 추가
                if (i != 0) {
                    recordInfo.append("\n"); // 첫 번째 항목이 아닌 경우 개행 추가
                }
                recordInfo.append(adjustedTimeKST).append(" ");   // 실제 시간
                recordInfo.append(elapseTime).append(" "); // 누적 시간
                recordInfo.append(speakerName).append(" ");
                recordInfo.append(text).append(" ");
            } else {
                recordInfo.append(text).append(" ");
            }

            preSpeakerName = speakerName;
        }

        return recordInfo.toString();
    }

    private ResponseEntity<String> requestSttToNaverCloud(File tempFile) {
        String url = apiUrl + "/recognizer/upload";

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("X-CLOVASPEECH-API-KEY", secretKey);

        // 요청 본문 생성
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        log.info(tempFile.getName());
        body.add("media", new FileSystemResource(tempFile));

        JSONObject jsonParams = new JSONObject()
                .put("language", "ko-KR")
                .put("completion", "sync")
                .put("callback", JSONObject.NULL)
                .put("wordAlignment", true)
                .put("fullText", true)
                .put("resultToObs", false)
                .put("noiseFiltering", true);

        body.add("params", jsonParams.toString());

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);

        log.info("RestTemplate 시작");
        return new RestTemplate().postForEntity(url, httpEntity, String.class);
    }
}