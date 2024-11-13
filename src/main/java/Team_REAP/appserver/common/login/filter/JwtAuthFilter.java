package Team_REAP.appserver.common.login.filter;

import Team_REAP.appserver.common.login.dto.SecurityUserDto;
import Team_REAP.appserver.common.login.exception.JwtException;
import Team_REAP.appserver.DB.mySQL.entity.Member;
import Team_REAP.appserver.DB.mySQL.repository.MemberRepository;
import Team_REAP.appserver.common.login.util.JwtUtil;
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

        // 토큰이 없으면 필터 체인 통과
        if (!StringUtils.hasText(atc)) {
            doFilter(request, response, filterChain);
            return;
        }

        // AccessToken을 검증하고, 만료되었을 경우 예외 발생
        log.info("JwtAuthFilter : AccessToken 검증");
        if (!jwtUtil.verifyToken(atc)) {
            log.info("verifyToken out");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Access Token 만료!");
            return;
        }

        log.info("JwtAuthFilter : AccessToken 값 있음");
        // AccessToken의 값이 있고, 유효한 경우에 진행한다.
        Member findMember = memberRepository.findByKakaoId(jwtUtil.getUid(atc))
                .orElseThrow(() -> new IllegalStateException("유저 정보를 찾을 수 없습니다."));

        log.info("JwtAuthFilter - findMember: {}", findMember.toString());
        SecurityUserDto userDto = SecurityUserDto.builder()
                .memberNo(findMember.getMemberNo())
                .id(findMember.getKakaoId())
                .role("ROLE_".concat(findMember.getUserRole()))
                .nickname(findMember.getNickname())
                .build();
        log.info("시큐리티 등록 user 객체 생성 성공");

        // SecurityContext에 인증 객체를 등록
        Authentication auth = getAuthentication(userDto);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }



    public Authentication getAuthentication(SecurityUserDto member) {
        log.info("JwtAuthFilter : getAuthentication");
        return new UsernamePasswordAuthenticationToken(member, "",
                List.of(new SimpleGrantedAuthority(member.getRole())));
    }

}
