package com.alex.spring.security.demo.controllers.dto;

import com.alex.spring.security.demo.persistence.entity.Especialidad;
import jakarta.validation.constraints.NotNull;

public record EditProviderRequest(

        Integer idProveedor,
        String name,
        String email,
        String password,
        Especialidad especialidad,
        Float calificacion,
        @NotNull Boolean isEnabled,
        @NotNull Boolean isCredentialNoExpired,
        @NotNull Boolean isAccountNoLocked,
        @NotNull Boolean isAccountNoExpired
) {
}
