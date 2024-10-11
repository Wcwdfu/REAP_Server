package Team_REAP.appserver.common.Config;

import Team_REAP.appserver.common.login.jwt.JwtAuthenticationFilter;
import Team_REAP.appserver.common.login.jwt.JwtTokenProvider;
import Team_REAP.appserver.common.login.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2Service oAuth2Service; // OAuth2 로그인 서비스
    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성 및 검증 클래스

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // JWT 필터 추가
        http
                .csrf().disable() // CSRF 비활성화
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을 사용하지 않음 (JWT로 상태 관리)
                .and()
                .authorizeRequests()
                .requestMatchers("/api/common/login/**", "/api/common/token/refresh").permitAll() // 로그인 및 토큰 재발급 요청은 인증 없이 접근 가능
                .anyRequest().authenticated() // 나머지 요청은 인증 필요
                .and()
                .oauth2Login()
                .loginPage("/api/common/login/kakao") // 카카오 로그인 페이지 경로
                .defaultSuccessUrl("/api/common/login/info", true) // 로그인 성공 후 리디렉트 경로
                .userInfoEndpoint()
                .userService(oAuth2Service); // OAuth2 로그인 후 사용자 정보 처리

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
