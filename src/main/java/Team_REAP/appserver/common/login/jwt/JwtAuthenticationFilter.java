//package Team_REAP.appserver.common.login.jwt;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.Collections;
//
//
//@Slf4j
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JwtTokenProvider tokenProvider; // JWT 토큰을 생성 및 검증하는 클래스
//
//    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
//        log.info("Filter call");
//        this.tokenProvider = tokenProvider;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//        log.info("do filter internal");
//        // 요청 헤더에서 JWT를 추출
//        String jwt = getJwtFromRequest(request);
//
//        // JWT 유효성 검증
//        if (jwt != null && tokenProvider.validateToken(jwt)) {
//            String userEmail = tokenProvider.getUserEmailFromJwt(jwt);
//
//            // 사용자 정보를 바탕으로 Authentication 객체 생성
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(userEmail, null, Collections.emptyList());
//            // SecurityContextHolder에 인증 객체 저장
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        }
//
//        filterChain.doFilter(request, response); // 다음 필터로 요청 전달
//    }
//
//    // Authorization 헤더에서 JWT를 추출
//    private String getJwtFromRequest(HttpServletRequest request) {
//        log.info("getJwtFromRequest");
//        String bearerToken = request.getHeader("Authorization");
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7); // "Bearer " 이후의 실제 JWT 반환
//        }
//        return null; // JWT가 없으면 null 반환
//    }
//}
