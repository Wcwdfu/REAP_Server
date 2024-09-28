//package Team_REAP.appserver.common.Config;
//
//import Team_REAP.appserver.common.login.service.OAuth2Service;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final OAuth2Service OAuth2Service;
//
//    @Bean
//    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
//        return http.csrf().disable() // csrf 보안 설정 사용 X
//                .logout().disable() // 로그아웃 사용 X
//                .formLogin().disable() // 폼 로그인 사용 X
//
//                .authorizeRequests() // 사용자가 보내는 요청에 인증 절차 수행 필요
//                .requestMatchers("/").permitAll() // 해당 URL은 인증 절차 수행 생략 가능
//                .anyRequest().authenticated() // 나머지 요청들은 모두 인증 절차 수행해야함
//
//                .and()
//                .oauth2Login() // OAuth2를 통한 로그인 사용
//                .loginPage("/api/common/login/kakao") // 커스텀 로그인 URL 설정
//                .defaultSuccessUrl("/api/common/login/info", true) // 로그인 성공시 이동할 URL
//                .userInfoEndpoint() // 사용자가 로그인에 성공하였을 경우,
//                .userService(OAuth2Service) // 해당 서비스 로직을 타도록 설정
//
//                .and()
//                .and().build();
//
//    }
//}