package Team_REAP.appserver.common.login.repository;

import Team_REAP.appserver.common.login.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // 사용자 이메일로 리프레시 토큰 조회
    Optional<RefreshToken> findByUserEmail(String userEmail);
}
