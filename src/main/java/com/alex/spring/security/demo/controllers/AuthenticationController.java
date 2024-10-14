package com.alex.spring.security.demo.controllers;


import com.alex.spring.security.demo.controllers.dto.AuthLoginRequest;
import com.alex.spring.security.demo.controllers.dto.AuthRegisterRequest;
import com.alex.spring.security.demo.controllers.dto.AuthRegisterResponse;
import com.alex.spring.security.demo.controllers.dto.AuthResponse;
import com.alex.spring.security.demo.services.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final UserDetailsServiceImpl userDetailService;

    private AuthenticationManager authenticationManager;

    public AuthenticationController(UserDetailsServiceImpl userDetailService,AuthenticationManager authenticationManager) {
        this.userDetailService = userDetailService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/log-in")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthLoginRequest userRequest,  HttpServletRequest request){
        return new ResponseEntity<>(this.userDetailService.loginUser(userRequest,request,authenticationManager), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthRegisterResponse> register(@RequestBody @Valid AuthRegisterRequest userRequest, HttpServletRequest request){
        return new ResponseEntity<>(this.userDetailService.registerUserAsClient(userRequest), HttpStatus.OK);
    }
}
