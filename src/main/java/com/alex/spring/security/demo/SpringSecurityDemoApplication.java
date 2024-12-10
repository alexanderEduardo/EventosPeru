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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@EnableScheduling
@SpringBootApplication
public class SpringSecurityDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityDemoApplication.class, args);
    }

}
