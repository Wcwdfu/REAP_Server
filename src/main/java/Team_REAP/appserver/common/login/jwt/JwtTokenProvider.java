//package Team_REAP.appserver.common.login.jwt;
//
//import Team_REAP.appserver.common.login.domain.RefreshToken;
//import Team_REAP.appserver.common.login.repository.RefreshTokenRepository;
//import io.jsonwebtoken.JwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class JwtTokenProvider {
//
//    // JWT 생성 시 사용되는 시크릿 키
//    @Value("${jwt.secret}")
//    private String JWT_SECRET;
//
//    // 액세스 토큰 유효기간 (7일)
//    private final long JWT_ACCESS_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;
//
//    // 리프레시 토큰 유효기간 (1년)
//    private final long JWT_REFRESH_EXPIRATION_MS = 365 * 24 * 60 * 60 * 1000L;
//
//    private final RefreshTokenRepository refreshTokenRepository; // 리프레시 토큰 저장소
//
//    // 액세스 토큰 생성 메서드
//    public String createAccessToken(String userEmail) {
//        log.info("createAccessToken");
//        return Jwts.builder()
//                .setSubject(userEmail) // 사용자 이메일을 subject로 설정
//                .setIssuedAt(new Date()) // 발급 시간 설정
//                .setExpiration(new Date((new Date()).getTime() + JWT_ACCESS_EXPIRATION_MS)) // 만료 시간 설정
//                .signWith(SignatureAlgorithm.HS512, JWT_SECRET) // 시크릿 키로 서명
//                .compact(); // 토큰 생성 및 반환
//    }
//
//    // 리프레시 토큰 생성 및 저장 메서드
//    public String createRefreshToken(String userEmail) {
//        String refreshToken = Jwts.builder()
//                .setSubject(userEmail) // 사용자 이메일을 subject로 설정
//                .setIssuedAt(new Date()) // 발급 시간 설정
//                .setExpiration(new Date((new Date()).getTime() + JWT_REFRESH_EXPIRATION_MS)) // 만료 시간 설정
//                .signWith(SignatureAlgorithm.HS512, JWT_SECRET) // 시크릿 키로 서명
//                .compact(); // 토큰 생성 및 반환
//        log.info("createRefreshToken");
//        // 기존 리프레시 토큰이 있으면 덮어쓰고, 없으면 새로 저장
//        RefreshToken tokenEntity = refreshTokenRepository.findByUserEmail(userEmail)
//                .orElse(new RefreshToken(userEmail, refreshToken));
//
//        tokenEntity.setToken(refreshToken);
//        refreshTokenRepository.save(tokenEntity); // 리프레시 토큰 저장
//
//        return refreshToken; // 리프레시 토큰 반환
//    }
//
//    // JWT에서 사용자 이메일 추출
//    public String getUserEmailFromJwt(String token) {
//        log.info("getUserEmailFromJwt");
//        return Jwts.parser()
//                .setSigningKey(JWT_SECRET) // 시크릿 키로 토큰 파싱
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject(); // subject에서 사용자 이메일 추출
//    }
//
//    // JWT 유효성 검증
//    public boolean validateToken(String authToken) {
//        log.info("validateToken");
//        try {
//            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(authToken); // 시크릿 키로 토큰 검증
//            return true; // 유효하면 true 반환
//        } catch (JwtException | IllegalArgumentException e) {
//            return false; // 유효하지 않으면 false 반환
//        }
//    }
//
//    // 리프레시 토큰 유효성 검증
//    public boolean validateRefreshToken(String refreshToken) {
//        log.info("validateRefreshToken");
//        try {
//            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(refreshToken); // 시크릿 키로 리프레시 토큰 검증
//            return true; // 유효하면 true 반환
//        } catch (JwtException | IllegalArgumentException e) {
//            return false; // 유효하지 않으면 false 반환
//        }
//    }
//}
