package Team_REAP.appserver.Repository;

import Team_REAP.appserver.Entity.LoginUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginRepository extends JpaRepository<LoginUser, Long> {

    Optional<LoginUser> findUserByEmailAndProvider(String email, String provider);
}