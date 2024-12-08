package com.alex.spring.security.demo.controllers.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudFormDTO {

    @NotBlank(message = "El nombre no puede estar vacío.")
    private String nombre;

    @NotBlank(message = "El correo no puede estar vacío.")
    @Email(message = "El formato del correo es inválido.")
    private String email;

    @NotBlank(message = "El teléfono no puede estar vacío.")
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe contener 9 dígitos.")
    private String telefono;

    @NotNull(message = "La fecha del evento es obligatoria.")
    @FutureOrPresent(message = "La fecha del evento no puede ser anterior al día de hoy.")
    private LocalDate fechaEvento;

    @NotNull(message = "La cantidad de invitados es obligatoria.")
    @Positive(message = "La cantidad de invitados debe ser mayor a 0.")
    private Integer cantidadInvitados;

    private List<Integer> serviciosSeleccionados; // IDs de los servicios adicionales seleccionados

    private String comentarios;

    @NotNull(message = "El local es obligatorio.")
    private Integer localId;
}
