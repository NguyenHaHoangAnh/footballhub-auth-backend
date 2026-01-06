package com.example.footballhub_auth_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    @NotNull
    private String username;

    @NotNull
    private String password;

    @NotNull
    private String confirmPassword;
}
