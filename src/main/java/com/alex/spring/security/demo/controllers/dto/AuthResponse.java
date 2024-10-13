package com.alex.spring.security.demo.controllers.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"username","message","jwt","status"})
public record AuthResponse(
        String jwt,
        String username,
        String message,
        boolean status
) {
}
