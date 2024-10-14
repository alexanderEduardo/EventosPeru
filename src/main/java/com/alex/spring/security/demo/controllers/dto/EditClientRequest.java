package com.alex.spring.security.demo.controllers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EditClientRequest(
        @NotNull Integer id,
        @NotBlank String name,
        @NotBlank String lastname,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String address,
        @NotBlank String phone,
        @NotNull Boolean isEnabled,
        @NotNull Boolean isCredentialNoExpired,
        @NotNull Boolean isAccountNoLocked,
        @NotNull Boolean isAccountNoExpired
) { }
