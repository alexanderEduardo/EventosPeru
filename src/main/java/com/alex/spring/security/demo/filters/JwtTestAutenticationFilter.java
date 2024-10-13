package com.alex.spring.security.demo.filters;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtTestAutenticationFilter extends UsernamePasswordAuthenticationFilter {
    String SPRING_SECURITY_FORM_USERNAME_KEY = "?????";

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        return  request.getParameter("usuario");
    }
}
