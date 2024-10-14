package com.alex.spring.security.demo.controllers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetClientResponse {

    @NotNull
    private Integer id;

    @NotBlank
    private String name;

    @NotBlank
    private String lastname;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String address;

    @NotBlank
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener exactamente 9 dígitos y ser numérico.")
    private String phone;

    @NotNull
    private Boolean isEnabled;

    @NotNull
    private Boolean isCredentialNoExpired;

    @NotNull
    private Boolean isAccountNoLocked;

    @NotNull
    private Boolean isAccountNoExpired;
}
