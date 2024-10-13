package com.alex.spring.security.demo.controllers;


import com.alex.spring.security.demo.controllers.dto.AuthCreateUserRequest;
import com.alex.spring.security.demo.controllers.dto.AuthLoginRequest;
import com.alex.spring.security.demo.controllers.dto.AuthResponse;
import com.alex.spring.security.demo.services.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final UserDetailsServiceImpl userDetailService;

    public AuthenticationController(UserDetailsServiceImpl userDetailService) {
        this.userDetailService = userDetailService;
    }

    @PostMapping("/log-in")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthLoginRequest userRequest){
        return new ResponseEntity<>(this.userDetailService.loginUser(userRequest), HttpStatus.OK);
    }


    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid AuthCreateUserRequest authCreateUser){
        return new ResponseEntity<>(this.userDetailService.createUser(authCreateUser),HttpStatus.CREATED);
    }
}
