package com.alex.spring.security.demo.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name= "proveedor")
public class Proveedor implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProveedor;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id")
    @Valid
    private UserEntity usuario;

    @ManyToOne
    @JoinColumn(name = "id_especialidad", referencedColumnName = "id")
    @Valid
    @NotNull
    private Especialidad especialidad;

    @Column(precision = 2)
    @NotNull
    private Float calificacion;
}
