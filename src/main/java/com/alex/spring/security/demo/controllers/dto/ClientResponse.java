package com.alex.spring.security.demo.controllers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientResponse(
        @NotBlank Integer id,
        @NotBlank String name,
        @NotBlank String lastname,
        @NotBlank @Email String email,
        @NotBlank String address,
        @NotBlank String phone
) { }