package com.example.footballhub_auth_backend.service;

import com.example.core.message.ResponseMsg;
import com.example.footballhub_auth_backend.dto.LoginRequestDto;

public interface AuthService {
    ResponseMsg<?> login(LoginRequestDto requestDto) throws Exception;
}
