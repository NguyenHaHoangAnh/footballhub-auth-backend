package com.example.footballhub_auth_backend.service.impl;

import com.example.core.message.ResponseMsg;
import com.example.footballhub_auth_backend.constant.AuthType;
import com.example.footballhub_auth_backend.constant.ResultCode;
import com.example.footballhub_auth_backend.dto.LoginRequestDto;
import com.example.footballhub_auth_backend.dto.RegisterRequestDto;
import com.example.footballhub_auth_backend.dto.TokenDto;
import com.example.footballhub_auth_backend.dto.UserDto;
import com.example.footballhub_auth_backend.model.User;
import com.example.footballhub_auth_backend.repository.UserRepository;
import com.example.footballhub_auth_backend.service.AuthService;
import com.example.footballhub_auth_backend.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    private PasswordEncoder passwordEncoder;

    private ModelMapper modelMapper;

    public TokenDto generateToken(User user, int authType) throws Exception {
        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);
        TokenDto tokenDto;

        if (authType == AuthType.BEARER.getValue()) {
            tokenDto = TokenDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .type(AuthType.BEARER.name())
                    .info(modelMapper.map(user, UserDto.class))
                    .build();
        } else if (authType == AuthType.BASIC.getValue()) {
            tokenDto = TokenDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .type(AuthType.BASIC.name())
                    .info(modelMapper.map(user, UserDto.class))
                    .build();
        } else {
            throw new Exception(HttpStatus.UNAUTHORIZED.toString());
        }

        return tokenDto;
    }

    @Override
    public ResponseEntity<?> login(LoginRequestDto requestDto) throws Exception {
        try {
            Optional<User> optionalUser = this.userRepository.findByUsername(requestDto.getUsername());
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
                    return new ResponseEntity<>(ResultCode.Code.WRONG_PASSWORD, HttpStatus.INTERNAL_SERVER_ERROR);
                }

                // authType là int, giá trị mặc định = 0
                TokenDto tokenDto = generateToken(user, requestDto.getAuthType());

                if (tokenDto != null && tokenDto.getAccessToken() != null) {
                    return new  ResponseEntity<>(tokenDto, HttpStatus.OK);
                }

                return null;
            }

            return new ResponseEntity<>(ResultCode.Code.USERNAME_NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("[Login error] {}", requestDto.getUsername(), e);
            throw new Exception(e);
        }
    }

    @Override
    public ResponseMsg<?> register(RegisterRequestDto requestDto) throws Exception {
        try {
//            if (requestDto.getPassword().equals())

            Optional<User> optionalUser = this.userRepository.findByUsernameIgnoreCase(requestDto.getUsername());

            if (optionalUser.isPresent()) {
                return ResponseMsg.newResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResultCode.Code.USER_EXISTED);
            }

            User newUser = new User();
            newUser.setUsername(requestDto.getUsername());
            newUser.setPassword(passwordEncoder.encode(requestDto.getPassword()));
            return ResponseMsg.newOKResponse();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
}
