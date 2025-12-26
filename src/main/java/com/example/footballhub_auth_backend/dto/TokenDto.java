package com.example.footballhub_auth_backend.dto;

import com.example.footballhub_auth_backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDto {
    private String accessToken;

    private String refreshToken;

    private String type;

    private UserDto info;
}
