package com.project.leavemanagement.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SignatureException;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

@Component
@Getter
public class JwtUtil {

	@Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;
    
    @Value("${jwt.ref-expiration-ms}")
    private long jwtRefExpirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, String role,boolean tok) {
        Date now = new Date();
        Date expiry = (tok?new Date(now.getTime() + jwtExpirationMs):new Date(now.getTime() + jwtRefExpirationMs));
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key())
                .compact();
    }

    public String extractUsername(String token) throws SignatureException {
        try {
			return Jwts.parser()
			        .verifyWith((SecretKey) key())
			        .build()
			        .parseSignedClaims(token)
			        .getPayload()
			        .getSubject();
		}catch (Exception e) {
			throw e;
		}
    }

    public boolean validateToken(String token){
        try {
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
         Logger log = LoggerFactory.getLogger(getClass());
         log.error("JWT validation failed: {}", ex.getMessage());
           return false;
        }
    }
    
    public Date extractExpiry(String token) {
        return Jwts.parser().verifyWith((SecretKey) key()).build()
                .parseSignedClaims(token).getPayload().getExpiration();
    }
	
}
