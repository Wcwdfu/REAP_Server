package Team_REAP.appserver.DB.mySQL.service;

import Team_REAP.appserver.DB.mySQL.entity.Member;
import Team_REAP.appserver.DB.mySQL.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

//    public Optional<Member> findByEmail(String email) {
//        return memberRepository.findByEmail(email);
//    }

    public Member findByMemberNo(Integer memberNo) {
        return memberRepository.findById(memberNo)
                .orElseThrow(IllegalStateException::new);
    }

    // 닉네임 중복 확인 메서드
    public boolean isDuplicated(String nickname) {
        Optional<Member> findMember = memberRepository.findByNickname(nickname);
        return findMember.isPresent();
    }


}
