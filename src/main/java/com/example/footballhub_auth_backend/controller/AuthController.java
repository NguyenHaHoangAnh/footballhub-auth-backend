package com.example.footballhub_auth_backend.controller;

import com.example.core.message.ResponseMsg;
import com.example.footballhub_auth_backend.constant.Constant;
import com.example.footballhub_auth_backend.dto.LoginRequestDto;
import com.example.footballhub_auth_backend.dto.RegisterRequestDto;
import com.example.footballhub_auth_backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constant.ApiService.PREFIX)
public class AuthController {
    @Autowired
    private AuthService authService;

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @CrossOrigin("/**")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequestDto requestDto) throws Exception {
        return this.authService.login(requestDto);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @CrossOrigin("/**")
    @ResponseBody
    public ResponseMsg<?> register(@RequestBody RegisterRequestDto requestDto) throws Exception {
        return this.authService.register(requestDto);
    }
}
