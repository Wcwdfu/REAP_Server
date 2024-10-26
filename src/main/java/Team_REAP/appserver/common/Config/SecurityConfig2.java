//package Team_REAP.appserver.common.Config;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//@Configuration
//public class SecurityConfig2 {
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())  // CSRF 비활성화
//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll()  // 모든 요청에 대해 인증 없이 접근 가능
//                );
//        return http.build();
//    }
//}