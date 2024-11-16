package com.alex.spring.security.demo.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name= "Solicitud_Servicios")

public class SolicitudServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSolicitudServicio;

    @ManyToOne
    @JoinColumn(name = "id_solicitud", nullable = false)
    private Solicitud solicitud;

    @ManyToOne
    @JoinColumn(name = "id_local_servicio", nullable = false)
    private LocalServicio localServicio;

    @Column(name = "precio_estimado", precision = 10, scale = 2)
    private BigDecimal precioEstimado;
}
