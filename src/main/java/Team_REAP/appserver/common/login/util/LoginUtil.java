package Team_REAP.appserver.common.login.util;

import Team_REAP.appserver.DB.mySQL.entity.Member;
import Team_REAP.appserver.DB.mySQL.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static Team_REAP.appserver.common.login.constants.UserInfoConst.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUtil {

    private final MemberRepository memberRepository;


    /**
     * 새 사용자 등록 메서드
     *
     * @param memberInfo Kakao에서 가져온 사용자 정보
     * @return Member 엔티티
     */
    @NotNull
    public Member validateAndRegisterMember(Member memberInfo) {
        return memberRepository.findByKakaoId(memberInfo.getKakaoId())
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .kakaoId(memberInfo.getKakaoId())
                            .nickname(memberInfo.getNickname())
                            .userRole(DEFAULT_ROLE)
                            .loginApi(PROVIDER_KAKAO)
                            .status(STATUS_ACTIVE)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return memberRepository.save(newMember);
                });
    }
}
