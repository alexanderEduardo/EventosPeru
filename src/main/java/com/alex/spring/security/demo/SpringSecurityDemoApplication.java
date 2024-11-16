package com.alex.spring.security.demo;

import ch.qos.logback.core.net.server.Client;
import com.alex.spring.security.demo.persistence.entity.*;
import com.alex.spring.security.demo.repository.EspecialidadRepository;
import com.alex.spring.security.demo.repository.UserRepository;
import com.alex.spring.security.demo.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class SpringSecurityDemoApplication {

    public SpringSecurityDemoApplication(IUserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityDemoApplication.class, args);
    }

    final IUserService userService;
    final PasswordEncoder passwordEncoder;

    @Autowired
    EspecialidadRepository especialidadRepository;

}
