package com.example.footballhub_auth_backend.repository;

import com.example.core.repository.BaseRepo;
import com.example.footballhub_auth_backend.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends BaseRepo<RefreshToken, Long> {
    Optional<RefreshToken> findByUserId(Long userId);

    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
