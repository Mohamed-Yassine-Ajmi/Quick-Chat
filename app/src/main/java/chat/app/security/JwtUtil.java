package chat.app.security;

import chat.app.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;
@Component
public class JwtUtil {
    @Value("${jwt.expiration}")
    private long expiration;
    @Value("${jwt.secret}")
    private String secretKey;
    public String generateToken(User user){
        Date expirationDate=new Date(System.currentTimeMillis()+expiration);
        return Jwts.builder()
                .claim("userId", user.getId())      // add info to payload
                .claim("role", user.getRole())
                .subject(user.getEmail())     // usually email or userId
                .expiration(expirationDate)         // when does it expire
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))            // sign with secret
                .compact();               // builds the final token String
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject(); // email was stored as subject!
    }

    public boolean validateToken(String token) {
        try {
            extractEmail(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
