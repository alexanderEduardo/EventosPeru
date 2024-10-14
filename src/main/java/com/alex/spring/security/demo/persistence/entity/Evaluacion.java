package com.alex.spring.security.demo.persistence.entity;

import java.io.Serializable;
import jakarta.persistence.*;
import lombok.*;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name= "evaluacion")
public class Evaluacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer evaluacionID;

    @ManyToOne
    @JoinColumn(name = "ClienteID")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "ProveedorID")
    private Proveedor proveedor;

    private Integer calificacion;
    private String comentarioCliente;
}
