package com.alex.spring.security.demo.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import java.util.Arrays;

public class JwtCookieInterceptor implements ClientHttpRequestInterceptor {
    private final HttpServletRequest request;

    public JwtCookieInterceptor(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Obtener el valor de la cookie `jwt` del HttpServletRequest
        String jwtToken = null;
        if (request.getCookies() != null) {
            jwtToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwt".equals(cookie.getName()))
                    .map(cookie -> cookie.getValue())
                    .findFirst()
                    .orElse(null);
        }

        // Agregar la cookie `jwt` a la solicitud si existe
        if (jwtToken != null) {
            //httpRequest.getHeaders().add("Cookie", "jwt=" + jwtToken);
            httpRequest.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken);
        }

        return execution.execute(httpRequest, body);
    }

}
