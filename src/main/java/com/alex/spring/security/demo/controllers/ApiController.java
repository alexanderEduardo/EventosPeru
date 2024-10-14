package com.alex.spring.security.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/test01")
    public String hello()   {
        return "Hello World GET test01";
    }

    @GetMapping("/test-admin")
    public String helloAdmin()   {
        return "Hello World GET /test-admin";
    }
}
