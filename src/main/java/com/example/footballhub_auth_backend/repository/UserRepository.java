package com.example.footballhub_auth_backend.repository;

import com.example.core.repository.BaseRepo;
import com.example.footballhub_auth_backend.model.User;

import java.util.Optional;

public interface UserRepository extends BaseRepo<User, Long> {
    Optional<User> findByUsername(String username) throws Exception;

    Optional<User> findByUsernameIgnoreCase(String username) throws Exception;

    Optional<User> findByUserId(Long userId) throws Exception;
}
