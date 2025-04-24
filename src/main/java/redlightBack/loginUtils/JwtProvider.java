package redlightBack.loginUtils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtProvider {

    // 에러 로깅을 위해 로거 준비
    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    // JWT secret을 저장할 변수
    private final SecretKey secretKey;


    // 생성자 함수
    public JwtProvider(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 유효한 토큰인지 검증하는 함수
    public Boolean isValidToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("Token expired", e);
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token", e);
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token", e);
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature", e);
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty", e);
        }
        return false;
    }

    // Supabase 사용자 id (subject) 추출
    public String getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    // 유효한 토큰의 데이터를 읽는 함수
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
