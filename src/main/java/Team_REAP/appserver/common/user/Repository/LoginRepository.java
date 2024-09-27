package Team_REAP.appserver.common.user.Repository;

import Team_REAP.appserver.common.user.Entity.LoginUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginRepository extends JpaRepository<LoginUser, Long> {
    //My SQL 꺼
    Optional<LoginUser> findUserByEmailAndProvider(String email, String provider);
}