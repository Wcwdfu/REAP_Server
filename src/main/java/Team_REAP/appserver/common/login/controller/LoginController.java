package Team_REAP.appserver.common.login.controller;

import Team_REAP.appserver.common.login.constants.UserInfoConst;
import Team_REAP.appserver.common.login.dto.GeneratedToken;
import Team_REAP.appserver.common.login.dto.LoginResponse;
import Team_REAP.appserver.common.login.exception.KakaoApiException;
import Team_REAP.appserver.common.login.service.KakaoApiService;
import Team_REAP.appserver.common.login.service.RefreshTokenService;
import Team_REAP.appserver.DB.mySQL.entity.Member;
import Team_REAP.appserver.DB.mySQL.repository.MemberRepository;
import Team_REAP.appserver.common.login.util.JwtUtil;
import Team_REAP.appserver.common.login.util.LoginUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static Team_REAP.appserver.common.login.constants.UserInfoConst.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {
    private final KakaoApiService kakaoApiService;
    private final JwtUtil jwtUtil;
    private final LoginUtil loginUtil;

    @Operation(summary = "JWT 토큰 받기"
            ,description = "클라이언트가 액세스 토큰을 보내면 JWT 토큰을 생성해서 반환합니다.")
    @GetMapping("/api/oauth/kakao")
    public ResponseEntity<LoginResponse> kakaoCallback(@RequestParam String accessToken){
        log.info("login start");
        // 1. 사용자 정보 요청
        Member memberInfo = kakaoApiService.getKakaoMember(accessToken);
        log.info("LoginController - kakaoId: {}", memberInfo.getKakaoId());
        log.info("LoginController - nickname: {}", memberInfo.getNickname());

        // 2. 사용자 조회 및 회원가입 처리
        Member member = loginUtil.validateAndRegisterMember(memberInfo);

        // 3. JWT 생성
        GeneratedToken generatedToken = jwtUtil.generateToken(member.getKakaoId(), member.getUserRole());

        // 4. 응답 생성
        LoginResponse loginResponse = new LoginResponse(true, generatedToken.getAccessToken());
        return ResponseEntity.status(HttpStatus.SC_OK).body(loginResponse);
    }
}
