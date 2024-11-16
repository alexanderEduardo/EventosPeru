package com.alex.spring.security.demo.controllers.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseSolicitudPresupuesto(
        String nombre,
        String correo,
        LocalDate fechaEvento,
        BigDecimal presupuestoEstimado,
        String mensajeInformativo
) { }
