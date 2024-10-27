package Team_REAP.appserver.common.login.ano;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Value("${spring.jwt.secret}")
    private String secret;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @AuthUser 애노테이션이 있고, 파라미터 타입이 String이면 true 반환
        return parameter.getParameterAnnotation(AuthUser.class) != null
                && parameter.getParameterType().equals(String.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("AuthUserArgumentResolver - secretKey: {}", secretKey);

        // 헤더에서 Authorization 정보 추출
        String token = webRequest.getHeader("Authorization");
        log.info("AuthUserArgumentResolver - authorizationHeader: {}", token);
        if (token != null) {

            // 토큰에서 이메일 정보 추출
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey) // JWT 서명 검증 키
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("sub", String.class); // 토큰 안에서 sub 반환(지금은 id)
        }
        return null; // 이메일을 추출할 수 없으면 null 반환
    }
}
