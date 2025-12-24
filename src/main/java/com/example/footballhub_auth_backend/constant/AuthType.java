package com.example.footballhub_auth_backend.constant;

public enum AuthType {
    BEARER(0), BASIC(1), ALL(2);

    private Integer value;

    private AuthType(Integer value) {
        this.value = value;
    }
}
