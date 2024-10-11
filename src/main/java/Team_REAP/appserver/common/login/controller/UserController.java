package Team_REAP.appserver.common.login.controller;

import Team_REAP.appserver.common.login.domain.RefreshToken;
import Team_REAP.appserver.common.login.jwt.JwtTokenProvider;
import Team_REAP.appserver.common.login.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/common/login")
@RequiredArgsConstructor
public class UserController {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성 클래스
    private final RefreshTokenRepository refreshTokenRepository; // 리프레시 토큰 저장을 위한 리포지토리 추가

    @GetMapping("/info")
    public Map<String, Object> getJson(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal(); // OAuth2User 정보 가져오기
        Map<String, Object> attributes = oAuth2User.getAttributes(); // 카카오 사용자 정보
        String userEmail = (String) attributes.get("email"); // 사용자 이메일 추출

        // JWT 액세스 토큰 및 리프레시 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(userEmail);
        String refreshToken = jwtTokenProvider.createRefreshToken(userEmail);

        // 새로운 맵 생성 후 사용자 정보와 토큰 추가
        Map<String, Object> response = new HashMap<>(attributes);
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        // 리프레시 토큰을 DB에 저장
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserEmail(userEmail)
                .orElse(new RefreshToken(userEmail, refreshToken)); // 기존 토큰을 찾거나 새로 생성

        refreshTokenEntity.setToken(refreshToken); // 토큰 설정
        refreshTokenRepository.save(refreshTokenEntity); // DB에 리프레시 토큰 저장

        return response; // 클라이언트로 JWT 및 사용자 정보 반환
    }

}
