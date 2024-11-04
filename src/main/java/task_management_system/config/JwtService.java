package task_management_system.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    private static final long JWT_TOKEN_VALID_TIME = TimeUnit.HOURS.toMillis(1);

    @Value("${jwt.secret}")
    private String secret;

    public String extractUsername(String token) {
        return getTokenPayloads(token).getSubject();
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .signWith(generateSigningKey())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(
                        new Date(System.currentTimeMillis() +
                        JWT_TOKEN_VALID_TIME)
                )
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String userEmail = getTokenPayloads(token).getSubject();

        return userEmail.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    public long getExpirationTime() {
        return JWT_TOKEN_VALID_TIME;
    }

    private boolean isTokenExpired(String token) {
        Date expirationDate =  getTokenPayloads(token)
                .getExpiration();

        return expirationDate.before(new Date());
    }

    private SecretKey generateSigningKey() {
        byte[] keyInBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyInBytes);
    }

    private Claims getTokenPayloads(String token) {
        return Jwts.parser()
                .verifyWith(generateSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
