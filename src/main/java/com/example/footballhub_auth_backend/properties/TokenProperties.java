package com.example.footballhub_auth_backend.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class TokenProperties {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expire.access-token}")
    private Integer accessTokenExpire; // minutes

    @Value("${jwt.expire.refresh-token}")
    private Integer refreshTokenExpire; // days
}
