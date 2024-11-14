package Team_REAP.appserver.DB.mySQL.controller;

import Team_REAP.appserver.DB.mySQL.entity.Member;
import Team_REAP.appserver.DB.mySQL.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test")
public class MysqlTestController {

    private final MemberRepository memberRepository;

    public MysqlTestController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @PostMapping("/addDummy")
    public ResponseEntity<String> addDummyData() {
        // 더미 데이터 생성
        Member dummyMember = Member.builder()
                .kakaoId("dummy_kakao_id")
                .nickname("dummy_nickname")
                //.email("dummy@example.com")
                .userRole("USER")
                .loginApi("KAKAO")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 데이터 삽입
        memberRepository.save(dummyMember);

        return ResponseEntity.ok("Dummy data inserted successfully");
    }
}

