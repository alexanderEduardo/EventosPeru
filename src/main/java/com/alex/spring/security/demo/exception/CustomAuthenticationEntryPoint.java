package com.alex.spring.security.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.DisabledException;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json");
        // Verificar si el error es porque el usuario está deshabilitado
        if (authException instanceof DisabledException) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{ \"error\": \"Account Disabled\", \"message\": \"La cuenta de usuario está deshabilitada.\" }");
        } else {
            // En caso de otros errores de autenticación
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{ \"error\": \"Invalid credentials provided\", \"message\": \"Access Denied\" }");
        }

    }

}
