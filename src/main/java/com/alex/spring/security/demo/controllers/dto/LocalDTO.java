package com.alex.spring.security.demo.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalDTO {

    private Integer idLocal;
    @NotBlank
    private String nombreLocal;
    @NotBlank
    private String direccion;
    @NotBlank
    private String descripcion;
    @NotNull
    private Integer capacidad;
    @NotNull
    private BigDecimal precioBase;
    @NotNull
    private MultipartFile imagen;
}
