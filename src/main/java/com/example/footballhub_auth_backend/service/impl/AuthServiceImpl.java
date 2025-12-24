package com.example.footballhub_auth_backend.service.impl;

import com.example.core.message.ResponseMsg;
import com.example.footballhub_auth_backend.constant.ResultCode;
import com.example.footballhub_auth_backend.dto.LoginRequestDto;
import com.example.footballhub_auth_backend.model.User;
import com.example.footballhub_auth_backend.repository.AuthRepository;
import com.example.footballhub_auth_backend.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthRepository authRepository;

    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseMsg<?> login(LoginRequestDto requestDto) throws Exception {
        try {
            Optional<User> userOptional = this.authRepository.findByUsername(requestDto.getUsername());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
                    return ResponseMsg.newResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResultCode.Code.WRONG_PASSWORD);
                }
            }

            return ResponseMsg.newResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResultCode.Code.USERNAME_NOT_FOUND);
        } catch (Exception e) {
            log.error("[Login error]", requestDto.getUsername(), e);
            throw new Exception(e);
        }
    }
}
