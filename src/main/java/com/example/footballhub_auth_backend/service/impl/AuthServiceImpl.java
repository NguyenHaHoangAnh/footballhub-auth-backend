package com.example.footballhub_auth_backend.service.impl;

import com.example.core.message.ResponseMsg;
import com.example.footballhub_auth_backend.constant.AuthType;
import com.example.footballhub_auth_backend.constant.ResultCode;
import com.example.footballhub_auth_backend.dto.*;
import com.example.footballhub_auth_backend.model.RefreshToken;
import com.example.footballhub_auth_backend.model.User;
import com.example.footballhub_auth_backend.properties.TokenProperties;
import com.example.footballhub_auth_backend.repository.RefreshTokenRepository;
import com.example.footballhub_auth_backend.repository.UserRepository;
import com.example.footballhub_auth_backend.service.AuthService;
import com.example.footballhub_auth_backend.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TokenProperties tokenProperties;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private static final Integer minPasswordLength = 6;

    public TokenDto generateToken(User user, int authType, Long expiredDateMillis) throws Exception {
        String accessToken = tokenService.generateAccessToken(user, expiredDateMillis);
        TokenDto tokenDto;

        if (authType == AuthType.BEARER.getValue()) {
            tokenDto = TokenDto.builder()
                    .accessToken(accessToken)
                    .type(AuthType.BEARER.name())
                    .info(modelMapper.map(user, UserDto.class))
                    .build();
        } else if (authType == AuthType.BASIC.getValue()) {
            tokenDto = TokenDto.builder()
                    .accessToken(accessToken)
                    .type(AuthType.BASIC.name())
                    .info(modelMapper.map(user, UserDto.class))
                    .build();
        } else {
            log.error("[GenerateToken error] {}", user.getUsername());
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

                long now = System.currentTimeMillis();
                long accessExpireAt = now + tokenProperties.getAccessTokenExpire() * 60 * 1000;
                long refreshExpireAt = now + tokenProperties.getRefreshTokenExpire() * 24 * 60 * 60 * 1000;
                // authType là int, giá trị mặc định = 0
                TokenDto tokenDto = generateToken(user, requestDto.getAuthType(), accessExpireAt);
                // Tạo refresh token, lưu vào db
                String refreshToken = tokenService.generateRefreshToken(user, refreshExpireAt);
                Optional<RefreshToken> optionalRefreshToken = this.refreshTokenRepository.findByUserId(user.getUserId());
                if (optionalRefreshToken.isPresent()) {
                    RefreshToken existedRefreshToken = optionalRefreshToken.get();
                    existedRefreshToken.setRefreshToken(refreshToken);
                    existedRefreshToken.setExpiredAt(new Date(refreshExpireAt));
                    this.refreshTokenRepository.save(existedRefreshToken);
                } else {
                    RefreshToken newRefreshToken = new RefreshToken();
                    newRefreshToken.setRefreshToken(refreshToken);
                    newRefreshToken.setUserId(user.getUserId());
                    newRefreshToken.setExpiredAt(new Date(refreshExpireAt));
                    this.refreshTokenRepository.save(newRefreshToken);
                }

                // Set Http-only cookie
                HttpHeaders headers = new HttpHeaders();
                if (tokenDto != null && tokenDto.getAccessToken() != null) {
                    ResponseCookie cookie = ResponseCookie
                            .from("refreshToken", refreshToken)
                            .httpOnly(true)
//                            .secure(true)
                            .path("/")
//                            .sameSite("None")
                            .maxAge(Duration.ofDays(tokenProperties.getRefreshTokenExpire()))
                            .build();
                    headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

                    return new  ResponseEntity<>(tokenDto, headers, HttpStatus.OK);
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
            if (requestDto.getPassword().length() < minPasswordLength) {
                log.error("[Register error] {} {}", requestDto.getUsername(), ResultCode.Code.PASSWORD_MIN_LENGTH);
                return ResponseMsg.newResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResultCode.Code.PASSWORD_MIN_LENGTH);
            }

            if (!requestDto.getPassword().equals(requestDto.getConfirmPassword())) {
                log.error("[Register error] {} {}", requestDto.getUsername(), ResultCode.Code.PASSWORD_NOT_MATCH);
                return ResponseMsg.newResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResultCode.Code.PASSWORD_NOT_MATCH);
            }

            Optional<User> optionalUser = this.userRepository.findByUsernameIgnoreCase(requestDto.getUsername());

            if (optionalUser.isPresent()) {
                log.error("[Register error] {} {}", requestDto.getUsername(), ResultCode.Code.USER_EXISTED);
                return ResponseMsg.newResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResultCode.Code.USER_EXISTED);
            }

            User newUser = new User();
            newUser.setUsername(requestDto.getUsername());
            newUser.setPassword(passwordEncoder.encode(requestDto.getPassword()));
            this.userRepository.save(newUser);
            return ResponseMsg.newOKResponse();
        } catch (Exception e) {
            log.error("[Register error] {}", requestDto.getUsername(), e);
            throw new Exception(e);
        }
    }

    @Override
    public ResponseEntity<?> refresh(String refreshToken) throws Exception {
        try {
            if (refreshToken == null) {
                log.error("[refresh token is null]");
                return new ResponseEntity<>(ResultCode.Code.INVALID_REFRESH_TOKEN, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            TokenResponseDto tokenResponseDto = this.tokenService.validateToken(refreshToken, null, null);
            Optional<User> optionalUser = this.userRepository.findByUserId(tokenResponseDto.getInfo().getUserId());

            if (optionalUser.isEmpty()) {
                log.error("[user not found] {}", refreshToken);
                return new ResponseEntity<>(ResultCode.Code.INVALID_REFRESH_TOKEN, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            User user = optionalUser.get();

            Optional<RefreshToken> optionalRefreshToken = this.refreshTokenRepository.findByUserId(user.getUserId());
            if (optionalRefreshToken.isEmpty()) {
                log.error("[refresh token not existed] userId={} refreshToken={}", user.getUserId(), refreshToken);
                return new ResponseEntity<>(ResultCode.Code.INVALID_REFRESH_TOKEN, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            RefreshToken existedRefreshToken = optionalRefreshToken.get();
            if (existedRefreshToken.getRefreshToken() == null || !existedRefreshToken.getRefreshToken().equals(refreshToken)) {
                log.error("[refresh token not match] refreshToke={} existedRefreshToken={}", refreshToken, existedRefreshToken);
                return new ResponseEntity<>(ResultCode.Code.INVALID_REFRESH_TOKEN, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            long now = System.currentTimeMillis();
            long accessExpireAt = now + tokenProperties.getAccessTokenExpire() * 60 * 1000;
            TokenDto tokenDto = generateToken(user, 0, accessExpireAt);

            // Set Http-only cookie
            HttpHeaders headers = new HttpHeaders();
            if (tokenDto != null && tokenDto.getAccessToken() != null) {
                ResponseCookie cookie = ResponseCookie
                        .from("refreshToken", refreshToken)
                        .httpOnly(true)
//                            .secure(true)
                        .path("/")
//                            .sameSite("None")
                        .maxAge(Duration.ofDays(tokenProperties.getRefreshTokenExpire()))
                        .build();
                headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

                return new  ResponseEntity<>(tokenDto, headers, HttpStatus.OK);
            }

            log.error("[cannot create tokenDto] {}", refreshToken);
            return new ResponseEntity<>(ResultCode.Code.INVALID_REFRESH_TOKEN, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("[Refresh error] {}", refreshToken, e);
            throw new Exception(e);
        }
    }

    @Override
    public ResponseEntity<?> logout(String refreshToken) throws Exception {
        try {
            if (refreshToken == null) {
                log.error("[logout refresh token is null]");
                return new ResponseEntity<>(ResultCode.Code.INVALID_REFRESH_TOKEN, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            TokenResponseDto tokenResponseDto = this.tokenService.validateToken(refreshToken, null, null);
            Optional<User> optionalUser = this.userRepository.findByUserId(tokenResponseDto.getInfo().getUserId());

            if (optionalUser.isEmpty()) {
                log.error("[logout user not found] {}", refreshToken);
                return new ResponseEntity<>(ResultCode.Code.INVALID_REFRESH_TOKEN, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            User user = optionalUser.get();

            Optional<RefreshToken> optionalRefreshToken = this.refreshTokenRepository.findByUserId(user.getUserId());
            if (optionalRefreshToken.isEmpty()) {
                log.error("[logout refresh token not existed] userId={} refreshToken={}", user.getUserId(), refreshToken);
                return new ResponseEntity<>(ResultCode.Code.INVALID_REFRESH_TOKEN, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            RefreshToken existedRefreshToken = optionalRefreshToken.get();
            this.refreshTokenRepository.delete(existedRefreshToken);

            // Set Http-only cookie
            HttpHeaders headers = new HttpHeaders();
            ResponseCookie cookie = ResponseCookie
                    .from("refreshToken", null)
                    .httpOnly(true)
//                            .secure(true)
                    .path("/")
//                            .sameSite("None")
                    .maxAge(Duration.ofDays(tokenProperties.getRefreshTokenExpire()))
                    .build();
            headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

            return new  ResponseEntity<>("Logout successfully", headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[Logout error] {}", refreshToken, e);
            throw new Exception(e);
        }
    }
}
