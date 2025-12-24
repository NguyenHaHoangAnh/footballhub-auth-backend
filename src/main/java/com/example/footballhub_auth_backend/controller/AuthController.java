package com.example.footballhub_auth_backend.controller;

import com.example.core.message.ResponseMsg;
import com.example.footballhub_auth_backend.constant.Constant;
import com.example.footballhub_auth_backend.dto.LoginRequestDto;
import com.example.footballhub_auth_backend.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constant.ApiService.PREFIX)
@Slf4j
public class AuthController {
    @Autowired
    private AuthService authService;

    @CrossOrigin("/**")
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
    public ResponseMsg<?> login(@RequestBody LoginRequestDto requestDto) throws Exception {
        return this.authService.login(requestDto);
    }
}
