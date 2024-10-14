package com.alex.spring.security.demo.controllers.dto;

import com.alex.spring.security.demo.persistence.entity.Especialidad;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetProviderResponse {
    @NotNull Integer id;
    @NotBlank String name;
    @NotBlank String email;
    @Valid Especialidad especialidad;

    @NotNull Float calificacion;
    @NotNull Boolean isEnabled;
    @NotNull Boolean isCredentialNoExpired;
    @NotNull Boolean isAccountNoLocked;
    @NotNull Boolean isAccountNoExpired;


}
