package com.alex.spring.security.demo.persistence.entity;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name= "solicitudes")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSolicitud;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_local", nullable = false)
    private Local local;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDate fechaEvento;

    private Integer cantidadInvitados;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('RECIBIDO', 'PRESUPUESTO_ENVIADO', 'ESPERANDO_RESPUESTA_DEL_CLIENTE', 'CERRADO', 'CANCELADO') DEFAULT 'RECIBIDO'")
    private EstadoSolicitud estado = EstadoSolicitud.RECIBIDO;

    @Column(name = "fecha_creacion", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime fechaActualizacion;

    @Column(name = "presupuesto_final", precision = 10, scale = 2)
    private BigDecimal presupuestoFinal;

    private String correo;

    // Enum para el estado
    public enum EstadoSolicitud {
        RECIBIDO,
        PRESUPUESTO_ENVIADO,
        ESPERANDO_RESPUESTA_DEL_CLIENTE,
        CERRADO,
        CANCELADO
    }
}
