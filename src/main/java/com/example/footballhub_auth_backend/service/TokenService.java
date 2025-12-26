package com.example.footballhub_auth_backend.service;

import com.example.footballhub_auth_backend.dto.TokenResponseDto;
import com.example.footballhub_auth_backend.model.User;

public interface TokenService {
    String generateAccessToken(User user) throws Exception;

    String generateRefreshToken(User user) throws Exception;

    TokenResponseDto validateToken(String accessToken, String requestUrl, String requestMethod);
}
