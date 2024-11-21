package Team_REAP.appserver.common.login.service;

import Team_REAP.appserver.DB.mySQL.entity.Member;
import Team_REAP.appserver.common.login.exception.KakaoApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static Team_REAP.appserver.common.login.constants.UserInfoConst.*;

@Service
public class KakaoApiService {

    private final String userInfoUrl = "https://kapi.kakao.com/v2/user/me"; // 사용자 정보를 가져오는 API의 URL

    /**
     * 카카오 액세스 토큰으로 사용자 정보 가져오기
     *
     * @param accessToken 카카오 액세스 토큰
     * @return Member 추출한 사용자 정보
     */
    // 카카오 사용자 정보를 가져와 Member 객체로 변환하는 메서드
    public Member getKakaoMember(String accessToken) {
        JsonNode userInfo = fetchJsonKakaoUserInfo(accessToken);
        return extractKakaoUserInfo(userInfo);
    }

    /**
     * 카카오 액세스 토큰으로 Json 형태의 사용자 정보 가져오기
     *
     * @param accessToken 카카오 액세스 토큰
     * @return JsonNode 추출한 Json형태의 사용자 정보
     */
    private JsonNode fetchJsonKakaoUserInfo(String accessToken) {

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.postForEntity(userInfoUrl, request, String.class);

            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new KakaoApiException("카카오에서 사용자 정보를 얻어오는데 실패하였습니다.");
        }
    }

    /**
     * 카카오 액세스 토큰으로 Json 형태의 사용자 정보 가져오기
     *
     * @param userInfo Json 형태의 사용자 정보
     * @return Member Json에서 추출한 사용자 정보
     */
    // JsonNode에서 Member 객체로 매핑
    private Member extractKakaoUserInfo(JsonNode userInfo) {
        String kakaoId = userInfo.path("id").asText();
        //String email = userInfo.path("kakao_account").path("email").asText();
        String nickname = userInfo.path("properties").path("nickname").asText();

        //return new Member(kakaoId, nickname, email, DEFAULT_ROLE, PROVIDER_KAKAO, STATUS_ACTIVE, LocalDateTime.now(), LocalDateTime.now());
        return new Member(kakaoId, nickname, DEFAULT_ROLE, PROVIDER_KAKAO, STATUS_ACTIVE, LocalDateTime.now(), LocalDateTime.now());
    }
}
