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


    /**
     * 토큰 생성 메서드
     *
     * @param id Kakao에서 가져온 사용자 id
     * @param role 사용자 권한(사용자, 관리자)
     * @return GeneratedToken 생성된 토큰 객체
     */
    public GeneratedToken generateToken(String id, String role) {
        log.info("JwtUtil - generateToken");
        // refreshToken과 accessToken을 생성한다.
        String refreshToken = generateRefreshToken(id, role);
        String accessToken = generateAccessToken(id, role);

        // 토큰을 Redis에 저장한다.
        tokenService.saveTokenInfo(id, refreshToken, accessToken);

        return new GeneratedToken(accessToken, refreshToken);
    }


    /**
     * 리프레시 토큰 생성 메서드
     *
     * @param id Kakao에서 가져온 사용자 id
     * @param role 사용자 권한(사용자, 관리자)
     * @return String 커스텀 리프레시 토큰
     */
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

    /**
     * 액세스 토큰 생성 메서드
     *
     * @param id Kakao에서 가져온 사용자 id
     * @param role 사용자 권한(사용자, 관리자)
     * @return String 커스텀 액세스 토큰
     */
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

    /**
     * 리프레시 토큰 생성 메서드
     *
     * @param token Kakao에서 가져온 사용자 id
     * @return boolean 토큰 검증 결과 ( true: 성공, false: 실패)
     * @throws Exception 검증 실패 시 false 반환
     */
    public boolean verifyToken(String token) {

        try {
            log.info("JwtUtil - verifyToken 진입");
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey) // 비밀키를 설정하여 파싱한다.
                    .build()
                    .parseSignedClaims(token); // 주어진 토큰을 파싱하여 Claims 객체를 얻는다.

            return claims.getPayload()
                    .getExpiration()
                    .after(new Date());  // 만료 시간이 현재 시간 이후인지 확인하여 유효성 검사 결과를 반환
        } catch (Exception e) {
            log.info("Exception : {}", e.getMessage());
            return false;
        }
    }

    /**
     * jwt sub 추출 메서드
     *
     * @param token 커스텀 액세스 토큰
     * @return String jwt payload 내부 sub => id
     */
    public String getUid(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getSubject();
    }

    /**
     * jwt role 추출 메서드
     *
     * @param token 커스텀 액세스 토큰
     * @return String role 사용자 권한(사용자, 관리자)
     */
    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

}