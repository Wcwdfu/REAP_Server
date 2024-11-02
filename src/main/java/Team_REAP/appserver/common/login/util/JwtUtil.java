package Team_REAP.appserver.common.login.util;

import Team_REAP.appserver.common.login.dto.GeneratedToken;
import Team_REAP.appserver.common.login.properties.JwtProperties;
import Team_REAP.appserver.common.login.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;
    private final RefreshTokenService tokenService;
    private SecretKey secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));// StandardCharsets.UTF_8
    }


    public GeneratedToken generateToken(String id, String role) {
        log.info("JwtUtil - generateToken");
        // refreshToken과 accessToken을 생성한다.
        String refreshToken = generateRefreshToken(id, role);
        String accessToken = generateAccessToken(id, role);
        log.info("JwtUtil - accessToken: {}",accessToken);
        log.info("JwtUtil - refreshToken: {}",refreshToken);

        // 토큰을 Redis에 저장한다.
        tokenService.saveTokenInfo(id, refreshToken, accessToken);

        return new GeneratedToken(accessToken, refreshToken);
    }

    public String generateRefreshToken(String id, String role) {
        log.info("JwtUtil - generateRefreshToken");
        // 토큰의 유효 기간을 밀리초 단위로 설정.
        long refreshPeriod = 1000L * 60L * 60L * 24L * 14; // 2주


        // 현재 시간과 날짜를 가져온다.
        Date now = new Date();
        // 새로운 클레임 객체를 생성하고, 이메일과 역할(권한)을 셋팅
        Claims claims = Jwts.claims().subject(id).issuedAt(now).expiration(new Date(now.getTime() + refreshPeriod)).build();

        return Jwts.builder()
                // Payload를 구성하는 속성들을 정의한다.
                .claims(claims)
                // 지정된 서명 알고리즘과 비밀 키를 사용하여 토큰을 서명한다.
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }


    public String generateAccessToken(String id, String role) {
        log.info("JwtUtil - generateAccessToken");
        long tokenPeriod = 1000L * 60L * 60L; // 60분

        Date now = new Date();

        Claims claims = Jwts.claims().subject(id).issuedAt(now).expiration(new Date(now.getTime() + tokenPeriod)).build();

        log.info("JwtUtil - claim: id, role");

        return Jwts.builder()
                .claims(claims) // Claims 설정
                .signWith(secretKey, Jwts.SIG.HS256) // 서명 알고리즘과 비밀키 설정
                .compact(); // JWT 생성

    }


    public boolean verifyToken(String token) {

        try {
            log.info("JwtUtil - verifyToken 진입");
            log.info("JwtUtil - secretKey: {}", secretKey);
            log.info("JwtUtil - token: {}", token);


            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey) // 비밀키를 설정하여 파싱한다.
                    .build()
                    .parseSignedClaims(token); // 주어진 토큰을 파싱하여 Claims 객체를 얻는다.

            return claims.getPayload()
                    .getExpiration()
                    .after(new Date());  // 만료 시간이 현재 시간 이후인지 확인하여 유효성 검사 결과를 반환
        } catch (Exception e) {
            log.info("Exception : {}", e.getMessage());
            log.info("false");
            return false;
        }
    }


    // 토큰에서 payload안 sub를 추출한다.(=> id )
    public String getUid(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getSubject();
    }

    // 토큰에서 ROLE(권한)만 추출한다.
    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

}