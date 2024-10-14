package com.alex.spring.security.demo.persistence.entity;

import java.io.Serializable;
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

    private String nombreLocal;
    private String direccion;
    private Integer capacidad;
    private String descripcion;
    private Float precioBase;
    private String imagen;

    @OneToMany(mappedBy = "local")
    private Set<CalificacionLocal> calificaciones;

}
