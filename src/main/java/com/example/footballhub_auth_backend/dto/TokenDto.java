package com.example.footballhub_auth_backend.dto;

import com.example.footballhub_auth_backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto {
    private String accessToken;

    private String refreshToken;

    private String type;

    private User userInfo;
}
