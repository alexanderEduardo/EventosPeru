package com.alex.spring.security.demo.controllers.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"username","message","status"})
public record AuthRegisterResponse(
        String username,
        String message,
        boolean status
) { }
