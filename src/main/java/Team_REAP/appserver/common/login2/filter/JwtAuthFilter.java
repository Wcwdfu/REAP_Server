package Team_REAP.appserver.common.login2.filter;

import Team_REAP.appserver.common.login2.dto.SecurityUserDto;
import Team_REAP.appserver.common.login2.exception.JwtException;
import Team_REAP.appserver.common.login2.user.Member;
import Team_REAP.appserver.common.login2.user.MemberRepository;
import Team_REAP.appserver.common.login2.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.List;
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        String uri = request.getRequestURI();

        return uri.contains("token/") || uri.contains("/swagger-ui.html/**")
         ||uri.contains("/webjars/**") || uri.contains("/static/**");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // request Header에서 AccessToken을 가져온다.
        String atc = request.getHeader("Authorization");

        log.info("JwtAuthFilter : 토큰 검사 생략");
        // 토큰 검사 생략(모두 허용 URL의 경우 토큰 검사 통과)
        if (!StringUtils.hasText(atc)) {
            doFilter(request, response, filterChain);
            return;
        }

        //여기까진 됐음
        log.info("JwtAuthFilter : AccessToken 검증");
        //log.info("Header value: {}", atc);
        // AccessToken을 검증하고, 만료되었을경우 예외를 발생시킨다.
        if (!jwtUtil.verifyToken(atc)) {
            log.info("verifyToken out");
            throw new JwtException("Access Token 만료!");
        }
        //여기부터 시작
        log.info("JwtAuthFilter : AccessToken 값 있음");
        // AccessToken의 값이 있고, 유효한 경우에 진행한다.
        if (jwtUtil.verifyToken(atc)) {
            log.info("토큰 검증 다시 진입");
            // AccessToken 내부의 payload에 있는 email로 user를 조회한다. 없다면 예외를 발생시킨다 -> 정상 케이스가 아님
            Member findMember = memberRepository.findByEmail(jwtUtil.getUid(atc)) // mysql에서 유저정보 꺼내오기
                    .orElseThrow(IllegalStateException::new);

            log.info("JwtAuthFilter - findMember: {}", findMember.toString());

            log.info("유저 조회 성공");
            // SecurityContext에 등록할 User 객체를 만들어준다.
            SecurityUserDto userDto = SecurityUserDto.builder()
                    .memberNo(findMember.getMemberNo())
                    .email(findMember.getEmail())
                    .role("ROLE_".concat(findMember.getUserRole()))
                    .nickname(findMember.getNickname())
                    .build();
            log.info("시큐리티 등록 user 객체 생성 성공");
            // SecurityContext에 인증 객체를 등록해준다.
            Authentication auth = getAuthentication(userDto);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }



    public Authentication getAuthentication(SecurityUserDto member) {
        log.info("JwtAuthFilter : getAuthentication");
        return new UsernamePasswordAuthenticationToken(member, "",
                List.of(new SimpleGrantedAuthority(member.getRole())));
    }

}
