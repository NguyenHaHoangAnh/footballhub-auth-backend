package com.example.footballhub_auth_backend.constant;

import lombok.Getter;

@Getter
public enum AuthType {
    BEARER(0), BASIC(1), ALL(2);

    private int value; // int mặc định = 0, Integer mặc định là null

    private AuthType(int value) {
        this.value = value;
    }
}
