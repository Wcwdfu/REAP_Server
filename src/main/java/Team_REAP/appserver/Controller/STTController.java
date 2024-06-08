package Team_REAP.appserver.Controller;

import Team_REAP.appserver.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class STTController {
    @Value("${naver.cloud.invoke.url}")
    private String apiUrl;

    @Value("${naver.cloud.secret.key}")
    private String secretKey;

    private final UserService userService;

    @PostMapping("/recognize-url")
    public ResponseEntity<String> recognizeMediaFromURL(@RequestParam("media") MultipartFile media,
                                                        @RequestParam("date") String date,
                                                        @RequestParam("language") String language,
                                                        @RequestParam(value = "completion", required = false) String completion,
                                                        @RequestParam(value = "callback", required = false) String callback,
                                                        @RequestParam(value = "wordAlignment", required = false, defaultValue = "true") boolean wordAlignment,
                                                        @RequestParam(value = "fullText", required = false, defaultValue = "true") boolean fullText,
                                                        @RequestParam(value = "resultToObs", required = false, defaultValue = "false") boolean resultToObs,
                                                        @RequestParam(value = "noiseFiltering", required = false, defaultValue = "true") boolean noiseFiltering) {




        File tempFile = null;
        StringBuilder recordInfo = new StringBuilder();;
        try {

            /*
             * 메타 데이터를 통해서 음성 생성 시간 받아오기
             * */
            // MultipartFile을 임시 파일로 저장
            tempFile = Files.createTempFile("upload", media.getOriginalFilename()).toFile();
            media.transferTo(tempFile);

            // MP4 파일 메타데이터 읽기
            IsoFile isoFile = new IsoFile(tempFile.getAbsolutePath());
            MovieHeaderBox mvhd = isoFile.getBoxes(MovieHeaderBox.class, true).get(0);
            long creationTime = mvhd.getCreationTime().getTime();
            LocalDateTime creationDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(creationTime), ZoneOffset.UTC);

            // 시간대를 변환 (예: 한국 시간대로 변환)
            ZoneId zoneId = ZoneId.of("Asia/Seoul");
            LocalDateTime creationDateTimeKST = creationDateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId).toLocalDateTime();

            // 날짜와 시간 부분만 추출
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String creationDateKST = creationDateTimeKST.format(dateFormatter);
            String creationTimeKST = creationDateTimeKST.format(timeFormatter);

            System.out.println("Creation Date (KST): " + creationDateKST);
            System.out.println("Creation Time (KST): " + creationTimeKST);

            /*
             * GPT에게 요청해서 답변 받아오기
             * */
            String url = apiUrl + "/recognizer/upload";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.add("X-CLOVASPEECH-API-KEY", secretKey);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("media", new FileSystemResource(tempFile));

            Map<String, Object> params = new HashMap<>();
            params.put("language", language);
            params.put("completion", completion);
            params.put("callback", callback);
            params.put("wordAlignment", wordAlignment);
            params.put("fullText", fullText);
            params.put("resultToObs", resultToObs);
            params.put("noiseFiltering", noiseFiltering);

            // JSON 객체로 변환
            JSONObject jsonParams = new JSONObject(params);
            body.add("params", jsonParams.toString());

            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

            String responseBody = responseEntity.getBody();
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray segments = jsonObject.getJSONArray("segments");

            // 누적 시간을 저장할 변수
            Duration totalDuration = Duration.ZERO;

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

                userService.create(speakerName, adjustedDateKST, adjustedTimeKST, text);
            }



            System.out.println(recordInfo);

            return responseEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
