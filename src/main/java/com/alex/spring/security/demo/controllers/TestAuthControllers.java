package com.alex.spring.security.demo.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/method")
public class TestAuthControllers {

    @GetMapping("/hello")
    public String hello(){
        return "Hello World GET";
    }

    @GetMapping("/hello-secured")
    public String helloSecured(){
        return "Hello World Secured GET";
    }

    @PostMapping("/post")
    public String postMethod(){
        return "Hello World POST";
    }

    @PutMapping("/put")
    public String putMethod(){
        return "Hello World PUT";
    }

    @DeleteMapping("/delete")
    public String deleteMethod(){
        return "Hello World DELETE";
    }
}
