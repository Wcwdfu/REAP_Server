package Team_REAP.appserver.common.login.controller;

import Team_REAP.appserver.common.login.ano.AuthUser;
import Team_REAP.appserver.common.login.domain.RefreshToken;
import Team_REAP.appserver.common.login.dto.StatusResponseDto;
import Team_REAP.appserver.common.login.dto.TestTokenDto;
import Team_REAP.appserver.common.login.dto.TokenResponseStatus;
import Team_REAP.appserver.common.login.repository.RefreshTokenRepository;
import Team_REAP.appserver.common.login.service.RefreshTokenService;
import Team_REAP.appserver.common.login.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final RefreshTokenRepository tokenRepository;
    private final RefreshTokenService tokenService;
    private final JwtUtil jwtUtil;
    //private final EmitterRepository emitterRepository;

    @PostMapping("token/logout")
    public ResponseEntity<StatusResponseDto> logout(@RequestHeader("Authorization") final String accessToken) {
        log.info("AuthController - logout");
        tokenService.removeRefreshToken(accessToken);
        return ResponseEntity.ok(StatusResponseDto.addStatus(200));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<TokenResponseStatus> refresh(@RequestHeader("Authorization") final String accessToken) {

        log.info("AuthController : 액세스 토큰으로 Refresh 토큰 객체를 조회");
        // 액세스 토큰으로 Refresh 토큰 객체를 조회
        Optional<RefreshToken> refreshToken = tokenRepository.findByAccessToken(accessToken);
        log.info("AuthController - refreshToken: {}", refreshToken);

        log.info("AuthController : RefreshToken이 존재하고 유효하다면 실행");
        // RefreshToken이 존재하고 유효하다면 실행
        if (refreshToken.isPresent() && jwtUtil.verifyToken(refreshToken.get().getRefreshToken())) {
            // RefreshToken 객체를 꺼내온다.
            RefreshToken resultToken = refreshToken.get();
            log.info("AuthController - resultToken: {}", resultToken);
            // 권한과 아이디를 추출해 새로운 액세스토큰을 만든다.
            String newAccessToken = jwtUtil.generateAccessToken(resultToken.getId(), jwtUtil.getRole(resultToken.getRefreshToken()));
            log.info("AuthController - newAccessToken: {}", newAccessToken);
            // 액세스 토큰의 값을 수정해준다.
            resultToken.updateAccessToken(newAccessToken);
            log.info("AuthController - resultToken: {}", resultToken);
            tokenRepository.save(resultToken);
            // 새로운 액세스 토큰을 반환해준다.
            return ResponseEntity.ok(TokenResponseStatus.addStatus(200, newAccessToken));
        }

        log.info("AuthController : 유효하지 않음 400 오류");
        return ResponseEntity.badRequest().body(TokenResponseStatus.addStatus(400, null));
    }

}
