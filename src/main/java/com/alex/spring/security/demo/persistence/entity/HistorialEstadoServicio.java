package com.alex.spring.security.demo.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name= "historial_estado_servicio")
public class HistorialEstadoServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHistorial;

    @ManyToOne
    @JoinColumn(name = "id_servicio")
    private Servicio servicio;

    @Enumerated(EnumType.STRING)
    private Servicio.Estado estado;

    private LocalDateTime fechaCambio;

    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;
}
