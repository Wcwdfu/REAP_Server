package Team_REAP.appserver.STT.util;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
@Component
public class STTUtils {

    @Value("${naver.cloud.invoke.url}")
    private String apiUrl;

    @Value("${naver.cloud.secret.key}")
    private String secretKey;

    /**
     * 네이버 클라우드 Api 요청 메서드
     *
     * @param tempFile 임시 음성 파일
     * @return String String으로 된 Json형식의 STT결과
     */
    public String requestStt(File tempFile) {
        // 네이버 클라우드 STT 요청 로직 구현
        String url = apiUrl + "/recognizer/upload";

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("X-CLOVASPEECH-API-KEY", secretKey);

        // Json으로 요청 생성
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
        return new RestTemplate().postForEntity(url, httpEntity, String.class).getBody();
    }

    public String makeScript(String sttResult, LocalDateTime creationDateTimeKST) {
        // 스크립트 생성 로직 구현
        JSONObject jsonObject = new JSONObject(sttResult);
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
}
