package com.alex.spring.security.demo.controllers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AuthRegisterRequest(
        @NotBlank String name,
        @NotBlank String lastname,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String address,
        @NotBlank
        @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener exactamente 9 dígitos y ser numérico.")
        String phone,
        @NotNull Boolean isEnabled,
        @NotNull Boolean isCredentialNoExpired,
        @NotNull Boolean isAccountNoLocked,
        @NotNull Boolean isAccountNoExpired
) { }
