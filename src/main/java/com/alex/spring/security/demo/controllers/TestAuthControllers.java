package com.alex.spring.security.demo.controllers;

import com.alex.spring.security.demo.persistence.entity.UserEntity;
import com.alex.spring.security.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestAuthControllers {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/hello")
    public String hello()   {
        return "Hello World GET";
    }

    @GetMapping("/users")
    public List<UserEntity> getUsers()   {
        return (List<UserEntity>) userRepository.findAll();
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
