package com.example.footballhub_auth_backend.repository;

import com.example.core.repository.BaseRepo;
import com.example.footballhub_auth_backend.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends BaseRepo<User, Integer> {
    Optional<User> findByUsername(String username);
}
