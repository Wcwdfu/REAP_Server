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

    @Transactional
    public void saveTokenInfo(String id, String refreshToken, String accessToken) {
        tokenRepository.save(new RefreshToken(id, accessToken, refreshToken));
    }

    @Transactional
    public void removeRefreshToken(String accessToken) {
        log.info("RefreshTokenService - removeRefreshToken");
        RefreshToken token = tokenRepository.findByAccessToken(accessToken)
                .orElseThrow(IllegalArgumentException::new);
        tokenRepository.delete(token);
    }
}
