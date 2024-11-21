package Team_REAP.appserver.common.login.service;

import Team_REAP.appserver.common.login.domain.RefreshToken;
import Team_REAP.appserver.common.login.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository tokenRepository;

    /**
     * 커스텀 토큰 DB에 저장
     *
     * @param id 카카오 사용자 id
     * @param refreshToken 커스텀 리프레시 토큰
     * @param accessToken 커스텀 액세스 토큰
     */
    @Transactional
    public void saveTokenInfo(String id, String refreshToken, String accessToken) {
        tokenRepository.save(new RefreshToken(id, accessToken, refreshToken));
    }

    /**
     * 커스텀 토큰 DB에서 제거
     *
     * @param accessToken 커스텀 액세스 토큰
     */
    @Transactional
    public void removeRefreshToken(String accessToken) {
        log.info("RefreshTokenService - removeRefreshToken");
        RefreshToken token = tokenRepository.findByAccessToken(accessToken)
                .orElseThrow(IllegalArgumentException::new);
        tokenRepository.delete(token);
    }
}
