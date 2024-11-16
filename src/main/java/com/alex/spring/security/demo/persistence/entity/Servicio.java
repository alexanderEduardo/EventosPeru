package com.alex.spring.security.demo.persistence.entity;

import java.io.Serializable;
import jakarta.persistence.*;
import lombok.*;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name= "servicio")
public class Servicio implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idServicio;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 50)
    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Getters y setters

    public enum Estado {
        PENDIENTE, EN_PROCESO, FINALIZADO
    }

    public enum PresupuestoEstado {
        APROBADO, PENDIENTE
    }
}
