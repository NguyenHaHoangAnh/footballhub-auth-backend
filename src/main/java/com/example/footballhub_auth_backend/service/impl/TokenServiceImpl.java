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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class TokenServiceImpl implements TokenService {
    private TokenProperties tokenProperties;

    @Autowired
    private UserRepository userRepository;

    private ModelMapper modelMapper;

    private final SignatureAlgorithm ALGORITHM = SignatureAlgorithm.ES256;

    @Override
    public String generateAccessToken(User user) {
        // Calculate expire date
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime expireTime = currentTime.plusMinutes(tokenProperties.getAccessTokenExpire());
        // Set claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(Date.from(expireTime.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(Keys.hmacShaKeyFor(tokenProperties.getSecretKey().getBytes()), ALGORITHM)
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        // Calculate expire date
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime expireTime = currentTime.plusDays(tokenProperties.getRefreshTokenExpire());
        // Set claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(Date.from(expireTime.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(Keys.hmacShaKeyFor(tokenProperties.getSecretKey().getBytes()), ALGORITHM)
                .compact();
    }

    @Override
    public TokenResponseDto validateToken(String accessToken, String requestUrl, String requestMethod) {
        TokenResponseDto tokenResponseDto = TokenResponseDto.builder()
                .success(false)
                .forbidden(true)
                .build();

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(tokenProperties.getSecretKey().getBytes()))
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();

            Integer userId = ((Number) claims.get("userId")).intValue();
            Optional<User> optionalUser = userRepository.findById(userId);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                tokenResponseDto = TokenResponseDto.builder()
                        .success(true)
                        .forbidden(true)
                        .info(modelMapper.map(user, UserDto.class))
                        .build();
            }
        } catch (ExpiredJwtException expiredJwtException) {
            log.error("[access token expired] {}", accessToken, expiredJwtException);
        } catch (Exception e) {
            log.error("[access token error] {}", accessToken, e);
        }

        return tokenResponseDto;
    }
}
