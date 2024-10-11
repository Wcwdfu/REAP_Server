package Team_REAP.appserver.common.login.controller;

import Team_REAP.appserver.common.login.domain.RefreshToken;
import Team_REAP.appserver.common.login.dto.AccessTokenResponse;
import Team_REAP.appserver.common.login.dto.TokenRequest;
import Team_REAP.appserver.common.login.jwt.JwtTokenProvider;
import Team_REAP.appserver.common.login.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common/token")
@RequiredArgsConstructor
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider; // JWT 생성 및 검증 클래스
    private final RefreshTokenRepository refreshTokenRepository; // 리프레시 토큰 저장소

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody TokenRequest tokenRequest) {
        String refreshToken = tokenRequest.getRefreshToken();

        // 리프레시 토큰 검증
        if (jwtTokenProvider.validateRefreshToken(refreshToken)) {
            String userEmail = jwtTokenProvider.getUserEmailFromJwt(refreshToken);

            // 저장된 리프레시 토큰과 비교하여 유효성 검증
            RefreshToken storedToken = refreshTokenRepository.findByUserEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

            if (!storedToken.getToken().equals(refreshToken)) {
                return ResponseEntity.status(401).body("Invalid refresh token");
            }

            // 리프레시 토큰이 유효하면 새로운 액세스 토큰 발급
            String newAccessToken = jwtTokenProvider.createAccessToken(userEmail);
            return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
        } else {
            return ResponseEntity.status(401).body("Refresh token expired");
        }
    }
}