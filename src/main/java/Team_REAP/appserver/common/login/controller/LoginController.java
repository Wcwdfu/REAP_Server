package Team_REAP.appserver.common.login.controller;

import Team_REAP.appserver.common.login.dto.LoginResponse;
import Team_REAP.appserver.common.login.service.RefreshTokenService;
import Team_REAP.appserver.common.login.user.Member;
import Team_REAP.appserver.common.login.user.MemberRepository;
import Team_REAP.appserver.common.login.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final MemberRepository memberRepository;
    private final RefreshTokenService tokenService;
    private final JwtUtil jwtUtil;

    private final String userInfoUrl = "https://kapi.kakao.com/v2/user/me"; // 사용자 정보를 가져오는 API의 URL
    @GetMapping("/api/oauth/kakao")
    public ResponseEntity<LoginResponse> kakaoCallback(@RequestParam String accessToken){
        // 1. 사용자 정보 요청
        String kakaoId = getKakaoId(accessToken);
        String email = getKakaoEmail(accessToken);
        String nickname = getKakaoNickname(accessToken);
        log.info("LoginController - kakaoId: {}", kakaoId);
        log.info("LoginController - email: {}", email);
        log.info("LoginController - nickname: {}", nickname);


        // 2. 사용자 조회 및 회원가입 처리
        Member member = memberRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                    Member newMember = new Member(kakaoId, nickname, email, "USER", "KAKAO", "ACTIVE", LocalDateTime.now(), LocalDateTime.now());
                    return memberRepository.save(newMember);
                });

        // 3. JWT 생성
        String customAccessToken = jwtUtil.generateAccessToken(kakaoId, member.getUserRole());
        String customRefreshToken = jwtUtil.generateRefreshToken(kakaoId, member.getUserRole());
        tokenService.saveTokenInfo(kakaoId, customAccessToken, customRefreshToken);

        LoginResponse loginResponse = new LoginResponse(true, customAccessToken);


        return ResponseEntity.status(HttpStatus.SC_OK).body(loginResponse);
    }


    private String getKakaoId(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.postForEntity(userInfoUrl, request, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("id").asText(); // 사용자의 카카오 고유 ID 추출
        } catch (Exception e) {
            throw new RuntimeException("카카오에서 고유 id를 얻어오는데 실패하였습니다.", e);
        }
    }


    private String getKakaoEmail(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.postForEntity(userInfoUrl, request, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.path("kakao_account").path("email").asText(); // 사용자의 카카오 고유 ID 추출
        } catch (Exception e) {
            throw new RuntimeException("카카오에서 고유 email를 얻어오는데 실패하였습니다.", e);
        }
    }

    private String getKakaoNickname(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.postForEntity(userInfoUrl, request, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.path("properties").path("nickname").asText(); // 사용자의 카카오 고유 ID 추출
        } catch (Exception e) {
            throw new RuntimeException("카카오에서 고유 nickname 얻어오는데 실패하였습니다.", e);
        }
    }
}
