package com.example.footballhub_auth_backend.service;

import com.example.core.message.ResponseMsg;
import com.example.footballhub_auth_backend.dto.LoginRequestDto;
import com.example.footballhub_auth_backend.dto.RegisterRequestDto;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> login(LoginRequestDto requestDto) throws Exception;

    ResponseMsg<?> register(RegisterRequestDto requestDto) throws Exception;
}
