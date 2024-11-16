package com.alex.spring.security.demo.persistence.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name= "local")
public class Local implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idLocal;

    @Column(nullable = false, length = 100)
    private String nombreLocal;

    @Column(length = 255)
    private String direccion;

    private Integer capacidad;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    private String imagen;

    @OneToMany(mappedBy = "local")
    private Set<CalificacionLocal> calificaciones;

}
