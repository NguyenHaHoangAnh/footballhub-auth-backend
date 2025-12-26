package com.example.footballhub_auth_backend.controller;

import com.example.footballhub_auth_backend.constant.Constant;
import com.example.footballhub_auth_backend.dto.TokenRequestDto;
import com.example.footballhub_auth_backend.dto.TokenResponseDto;
import com.example.footballhub_auth_backend.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constant.ApiService.PREFIX)
public class ValidateController {
    @Autowired
    private TokenService tokenService;

    @RequestMapping(value = "/token", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @CrossOrigin("/**")
    @ResponseBody
    public TokenResponseDto validateToken(TokenRequestDto tokenRequestDto) {
        return tokenService.validateToken(tokenRequestDto.getAccessToken(), tokenRequestDto.getRequestUrl(), tokenRequestDto.getRequestMethod());
    }
}
