package com.example.footballhub_auth_backend.util;

import com.example.footballhub_auth_backend.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expire.access-token}")
    private static Integer accessTokenExpire;

    @Value("${jwt.expire.refresh-token}")
    private static Integer refreshTokenExpire;

    private final SignatureAlgorithm ALGORITHM = SignatureAlgorithm.ES256;

    public String generateAccessToken(User user) {
        // Calculate expire date
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime expireTime = currentTime.plusMinutes(accessTokenExpire);
        // Set claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(Date.from(expireTime.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), ALGORITHM)
                .compact();
    }

    public String generateRefreshToken(User user) {
        // Calculate expire date
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime expireTime = currentTime.plusDays(refreshTokenExpire);
        // Set claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(Date.from(expireTime.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), ALGORITHM)
                .compact();
    }
}
