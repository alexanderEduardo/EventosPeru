package com.alex.spring.security.demo.controllers.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudFormDTO {

    private String nombre;
    private String email;
    private String telefono;
    private LocalDate fechaEvento;
    private Integer cantidadInvitados;
    private List<Integer> serviciosSeleccionados; // IDs de los servicios adicionales seleccionados
    private String comentarios;
    private Integer localId;
}
