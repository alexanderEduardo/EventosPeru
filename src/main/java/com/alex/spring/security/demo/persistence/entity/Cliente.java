package com.alex.spring.security.demo.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name= "cliente")
public class Cliente implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCliente;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id")
    @Valid
    private UserEntity usuario;
    @NotBlank
    private String direccion;
    @NotBlank
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener exactamente 9 dígitos y ser numérico.")
    private String telefono;
    @NotBlank
    private String apellido;
}
