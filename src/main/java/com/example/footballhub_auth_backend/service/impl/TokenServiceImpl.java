package com.example.footballhub_auth_backend.service.impl;

import com.example.footballhub_auth_backend.dto.TokenResponseDto;
import com.example.footballhub_auth_backend.dto.UserDto;
import com.example.footballhub_auth_backend.model.User;
import com.example.footballhub_auth_backend.properties.TokenProperties;
import com.example.footballhub_auth_backend.repository.UserRepository;
import com.example.footballhub_auth_backend.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class TokenServiceImpl implements TokenService {
    @Autowired
    private TokenProperties tokenProperties;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    private final SignatureAlgorithm ALGORITHM = SignatureAlgorithm.HS256;

    @Override
    public String generateAccessToken(User user, Long expiredDateMillis) {
        // Calculate expire date
        long expireAt;
        expireAt = expiredDateMillis == null
                ? System.currentTimeMillis() + tokenProperties.getAccessTokenExpire() * 60 * 1000
                : expiredDateMillis;
        // Set claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(expireAt))
                .signWith(Keys.hmacShaKeyFor(tokenProperties.getSecretKey().getBytes()), ALGORITHM)
                .compact();
    }

    @Override
    public String generateRefreshToken(User user, Long expiredDateMillis) {
        // Calculate expire date
        long expireAt;
        expireAt = expiredDateMillis == null
                ? System.currentTimeMillis() + tokenProperties.getAccessTokenExpire() * 24 * 60 * 60 * 1000
                : expiredDateMillis;
        // Set claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(expireAt))
                .signWith(Keys.hmacShaKeyFor(tokenProperties.getSecretKey().getBytes()), ALGORITHM)
                .compact();
    }

    @Override
    public TokenResponseDto validateToken(String token, String requestUrl, String requestMethod) {
        TokenResponseDto tokenResponseDto = TokenResponseDto.builder()
                .success(false)
                .forbidden(true)
                .build();

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(tokenProperties.getSecretKey().getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = ((Number) claims.get("userId")).longValue();
            Optional<User> optionalUser = userRepository.findById(userId);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                tokenResponseDto = TokenResponseDto.builder()
                        .success(true)
                        .forbidden(false)
                        .info(modelMapper.map(user, UserDto.class))
                        .build();
            }
        } catch (ExpiredJwtException expiredJwtException) {
            log.error("[token] expired {}", token, expiredJwtException);
        } catch (Exception e) {
            log.error("[token] error {}", token, e);
        }

        return tokenResponseDto;
    }
}
