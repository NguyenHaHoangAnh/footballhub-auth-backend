package com.example.footballhub_auth_backend.constant;

public enum ResultCode {
    ;
    private final String code;

    ResultCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public interface Code {
        String USERNAME_NOT_FOUND = "USERNAME_NOT_FOUND";
        String WRONG_PASSWORD = "WRONG_PASSWORD";
        String USER_EXISTED = "USER_EXISTED";
    }
}
