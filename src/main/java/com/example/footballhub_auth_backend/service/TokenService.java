package com.example.footballhub_auth_backend.service;

import com.example.footballhub_auth_backend.dto.TokenResponseDto;
import com.example.footballhub_auth_backend.model.User;

public interface TokenService {
    String generateAccessToken(User user, Long expiredDateMillis) throws Exception;

    String generateRefreshToken(User user, Long expiredDateMillis) throws Exception;

    TokenResponseDto validateToken(String accessToken, String requestUrl, String requestMethod);
}
