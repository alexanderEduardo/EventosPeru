package com.alex.spring.security.demo.persistence.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaEvento;

    @ManyToOne
    @JoinColumn(name = "id_local")
    private Local local;

    private Float presupuesto;

    @Enumerated(EnumType.STRING)
    private PresupuestoEstado presupuestoAprobado;

    private Integer capacidad;

    // Getters y setters

    public enum Estado {
        PENDIENTE, EN_PROCESO, FINALIZADO
    }

    public enum PresupuestoEstado {
        APROBADO, PENDIENTE
    }
}
