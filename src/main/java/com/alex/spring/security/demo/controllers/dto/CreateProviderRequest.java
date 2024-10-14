package com.alex.spring.security.demo.controllers.dto;

import com.alex.spring.security.demo.persistence.entity.Especialidad;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProviderRequest(
        @NotBlank String name,
        @NotBlank String email,
        @NotNull String password,
        @Valid Especialidad especialidad,
        @NotNull Float calificacion,
        @NotNull Boolean isEnabled,
        @NotNull Boolean isCredentialNoExpired,
        @NotNull Boolean isAccountNoLocked,
        @NotNull Boolean isAccountNoExpired
) {
}
