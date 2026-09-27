package com.actisys.authservice.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.ttl-seconds:860000}")
    private long ttlSeconds;

    @Value("${jwt.issuer:actisys-auth}")
    private String issuer;

    @Value("${jwt.refresh-token-expire-mill}")
    private long refreshTokenExpireMill;

    private SecretKey key;
    private Duration ttl;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }


    public String generate(UUID userId, String username, Map<String, Object> extra) {
        Instant now = Instant.now();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("username", username);

        if (extra != null) {
            claims.putAll(extra);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(ttl)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generate(UUID userId, String username) {
        return generate(userId, username, null);
    }

    public Jws<Claims> verify(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    public String generateRefreshToken(UUID userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenExpireMill))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID extractUserId(String token) {
        Claims claims = verify(token).getPayload();
        String userIdStr = claims.get("userId", String.class);
        if (userIdStr == null) {
            // Fallback на subject для refresh токенов
            userIdStr = claims.getSubject();
        }
        return UUID.fromString(userIdStr);
    }

    public String extractUsername(String token) {
        Claims claims = verify(token).getPayload();
        return claims.get("username", String.class);
    }
}