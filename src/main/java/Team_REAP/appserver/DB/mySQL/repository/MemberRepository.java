package Team_REAP.appserver.DB.mySQL.repository;

import Team_REAP.appserver.DB.mySQL.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findByEmail(String email); // 보류

    Optional<Member> findByKakaoId(String id);

    Optional<Member> findByNickname(String nickname);
}
