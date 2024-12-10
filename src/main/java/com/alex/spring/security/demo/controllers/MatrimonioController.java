package com.alex.spring.security.demo.controllers;

import com.alex.spring.security.demo.controllers.dto.ResponseSolicitudPresupuesto;
import com.alex.spring.security.demo.controllers.dto.SolicitudFormDTO;
import com.alex.spring.security.demo.persistence.entity.*;
import com.alex.spring.security.demo.persistence.entity.utils.UserCustomDetails;
import com.alex.spring.security.demo.services.EmailService;
import com.alex.spring.security.demo.services.IUserService;
import com.alex.spring.security.demo.services.MatrimonioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class MatrimonioController {

    @Autowired
    MatrimonioService matrimonioService;

    @Autowired
    IUserService userService;

    @Autowired
    private EmailService emailService;

    protected final Log logger = LogFactory.getLog(this.getClass());
    @GetMapping(value = {"/public/locales"})
    public String verLocales(Model model) {
        logger.info("Accediendo a la vista pública de locales.");

        try {
            logger.debug("Recuperando la lista de locales desde el servicio.");
            List<Local> locales = matrimonioService.getLocales();
            model.addAttribute("locales", locales);
            logger.info("Lista de locales cargada exitosamente. Total de locales: " + locales.size());
        } catch (Exception e) {
            logger.error("Error al cargar la lista de locales.", e);
            model.addAttribute("error", "No se pudo cargar la lista de locales. Intente más tarde.");
        }

        return "locales";
    }


    @GetMapping(value = {"/public/proveedores"})
    public String verProveedores(Model model) {
        logger.info("Accediendo a la vista pública de proveedores.");

        try {
            logger.debug("Recuperando la lista de proveedores desde el servicio.");
            List<Proveedor> proveedores = matrimonioService.getProveedores();
            model.addAttribute("proveedores", proveedores);
            logger.info("Lista de proveedores cargada exitosamente. Total de proveedores: " + proveedores.size());
        } catch (Exception e) {
            logger.error("Error al cargar la lista de proveedores.", e);
            model.addAttribute("error", "No se pudo cargar la lista de proveedores. Intente más tarde.");
        }

        return "proveedores";
    }

    @GetMapping("matrimonio/solicitar-presupuesto/{idLocal}")
    public String solicitarPresupuesto(@PathVariable Integer idLocal, Model model, RedirectAttributes flash) {
        logger.info("Solicitando presupuesto para el local con ID: " + idLocal);

        try {
            logger.debug("Buscando el local con ID: " + idLocal);
            Optional<Local> localOptional = matrimonioService.findLocalById(idLocal);

            if (localOptional.isPresent()) {
                Local local = localOptional.get();
                logger.info("Local encontrado: " + local.getNombreLocal());

                logger.debug("Recuperando los servicios disponibles para el local con ID: " + idLocal);
                List<LocalServicio> serviciosDisponibles = matrimonioService.findByLocalService(local);

                model.addAttribute("local", local);
                model.addAttribute("serviciosDisponibles", serviciosDisponibles);
                logger.info("Servicios disponibles cargados exitosamente para el local con ID: " + idLocal);

                return "formularioPresupuesto";
            } else {
                logger.warn("No se encontró un local con el ID: " + idLocal);
                flash.addFlashAttribute("error", "El local solicitado no existe.");
            }
        } catch (Exception e) {
            logger.error("Error al procesar la solicitud de presupuesto para el local con ID: " + idLocal, e);
            flash.addFlashAttribute("error", "Ocurrió un error en el sistema. Intente más tarde.");
        }

        return "redirect:/home";
    }

    @PostMapping("matrimonio/procesar-solicitud-presupuesto")
    @ResponseBody
    public ResponseEntity<?> procesarSolicitudPresupuesto(@Valid @RequestBody SolicitudFormDTO solicitudForm, BindingResult result) {
        logger.info("Procesando solicitud de presupuesto.");

        try {
            // Extraer datos del formulario
            Integer cantidadInvitados = solicitudForm.getCantidadInvitados();
            String comentarios = solicitudForm.getComentarios();
            String email = solicitudForm.getEmail();
            String nombre = solicitudForm.getNombre();
            Integer localId = solicitudForm.getLocalId();
            String telefono = solicitudForm.getTelefono();
            LocalDate fechaEvento = solicitudForm.getFechaEvento();
            List<Integer> serviciosSeleccionados = solicitudForm.getServiciosSeleccionados();

            logger.debug("Datos de la solicitud recibidos: localId=" + localId + ", nombre=" + nombre + ", email=" + email);

            // Validar datos
            LocalDate fechaHoy = LocalDate.now();
            Local local = matrimonioService.findLocalById(localId)
                    .orElseThrow(() -> {
                        logger.error("Local no encontrado con ID: " + localId);
                        return new RuntimeException("Local no encontrado");
                    });

            logger.debug("Local encontrado: " + local.getNombreLocal() + ", capacidad=" + local.getCapacidad());

            if (cantidadInvitados != null && cantidadInvitados > local.getCapacidad()) {
                logger.warn("La cantidad de invitados excede la capacidad del local.");
                result.rejectValue("cantidadInvitados", null,
                        "La cantidad de invitados excede la capacidad del local (" + local.getCapacidad() + ")");
            }

            if (fechaEvento != null && fechaEvento.isBefore(fechaHoy)) {
                logger.warn("La fecha del evento es anterior al día de hoy.");
                result.rejectValue("fechaEvento", null, "La fecha del evento no puede ser anterior al día de hoy.");
            }

            // Verificar si hay errores de validación
            if (result.hasErrors()) {
                logger.warn("Errores de validación encontrados.");
                Map<String, String> errors = new HashMap<>();
                result.getAllErrors().forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

                logger.debug("Errores de validación: " + errors);

                Map<String, Object> errorDetails = Map.of(
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "errorType", "VALIDATION_ERROR",
                        "message", "Error de validación: algunos campos contienen errores",
                        "errors", errors
                );

                return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
            }

            // Obtener cliente actual
            Cliente cliente = getCurrentCustomer();
            logger.info("Cliente autenticado: " + cliente.getUsuario().getUsername());

            // Calcular presupuesto
            BigDecimal presupuestoFinalInicial = local.getPrecioBase();
            logger.debug("Precio base del local: " + presupuestoFinalInicial);

            for (Integer idLocalServicio : serviciosSeleccionados) {
                LocalServicio localServicio = matrimonioService.findByLocalService(local).stream()
                        .filter(ls -> ls.getIdLocalServicio().equals(idLocalServicio))
                        .findFirst()
                        .orElseThrow(() -> {
                            logger.error("Servicio no encontrado con ID: " + idLocalServicio);
                            return new RuntimeException("Servicio no encontrado");
                        });
                presupuestoFinalInicial = presupuestoFinalInicial.add(localServicio.getPrecio());
            }

            logger.info("Presupuesto final calculado: " + presupuestoFinalInicial);

            // Crear y guardar la solicitud
            Solicitud solicitud = matrimonioService.crearSolicitud(cliente, local, fechaEvento, cantidadInvitados, presupuestoFinalInicial, email);
            logger.info("Solicitud creada exitosamente con ID: " + solicitud.getIdSolicitud());

            // Guardar los servicios seleccionados
            matrimonioService.guardarServiciosParaSolicitud(solicitud, serviciosSeleccionados);
            logger.info("Servicios seleccionados asociados a la solicitud.");

            // Enviar correos de notificación
            logger.debug("Enviando correos de notificación.");
            emailService.enviarCorreo("admin.eventos_peru@yopmail.com", "Nueva solicitud de evento creada",
                    "Se ha creado una nueva solicitud de evento para el cliente " + nombre + " que tiene el correo " + email +
                            " y el teléfono " + telefono + "\n\n" + "Comentarios: " + comentarios);

            emailService.enviarCorreo(email, "Confirmación de solicitud de presupuesto",
                    "Gracias por solicitar un presupuesto. Pronto recibirá una respuesta.");
            logger.info("Correos enviados correctamente.");

            // Respuesta final
            return ResponseEntity.ok(new ResponseSolicitudPresupuesto(nombre, email, fechaEvento, presupuestoFinalInicial,
                    "Muchas gracias por crear la solicitud. Pronto recibirá un correo con la confirmación de su presupuesto."));
        } catch (Exception e) {
            logger.error("Error al procesar la solicitud de presupuesto.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "message", "Ocurrió un error al procesar su solicitud. Intente nuevamente."
            ));
        }
    }

    private Cliente getCurrentCustomer() {
        String emailFromCurrentUser = getEmailFromCurrentUser();
        Cliente cliente = userService.buscarClientePorEmail(emailFromCurrentUser);
        if(cliente == null){
            throw new RuntimeException("el cliente no fue encontrado");
        }
        return cliente;
    }

    private static String getEmailFromCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserCustomDetails details = (UserCustomDetails) authentication.getDetails();
        String emailFromCurrentUser = details.getEmail();
        return emailFromCurrentUser;
    }

    @PostMapping("/admin/actualizar-presupuesto/{id}")
    public String actualizarPresupuesto(@PathVariable Integer id, @RequestParam BigDecimal presupuestoFinal) {
        logger.info("Actualizando presupuesto para la solicitud con ID: " + id);

        try {
            Solicitud solicitud = matrimonioService.findSolicitudById(id);
            logger.debug("Solicitud encontrada: " + solicitud.getIdSolicitud() + ", estado actual: " + solicitud.getEstado());

            solicitud.setPresupuestoFinal(presupuestoFinal);
            solicitud.setEstado(Solicitud.EstadoSolicitud.PRESUPUESTO_ENVIADO);
            matrimonioService.saveSolicitud(solicitud);

            logger.info("Presupuesto actualizado exitosamente. Nuevo presupuesto: " + presupuestoFinal);

            emailService.enviarCorreo(solicitud.getCorreo(), "Confirmación de presupuesto",
                    "Gracias por solicitar agendar un evento para su matrimonio. Su presupuesto final es " + presupuestoFinal);
            logger.info("Correo de confirmación enviado al cliente: " + solicitud.getCorreo());
        } catch (Exception e) {
            logger.error("Error al actualizar el presupuesto para la solicitud con ID: " + id, e);
            return "redirect:/admin/solicitudes?error=true";
        }

        return "redirect:/admin/solicitudes";
    }


    // Página para revisar una solicitud específica
    @GetMapping("/admin/revisar-solicitud/{id}")
    public String revisarSolicitud(@PathVariable Integer id, Model model) {
        logger.info("Revisando la solicitud con ID: " + id);

        try {
            Solicitud solicitud = matrimonioService.findSolicitudById(id);
            logger.debug("Solicitud encontrada: " + solicitud.getIdSolicitud() + ", estado actual: " + solicitud.getEstado());

            model.addAttribute("solicitud", solicitud);
            return "revisarSolicitud";
        } catch (Exception e) {
            logger.error("Error al cargar la solicitud con ID: " + id, e);
            return "redirect:/admin/solicitudes?error=true";
        }
    }


    @GetMapping("/cliente/ver-solicitudes")
    public String verSolicitudesDelCliente(Model model, RedirectAttributes flash) {
        logger.info("Accediendo al listado de solicitudes del cliente.");

        try {
            Cliente cliente = getCurrentCustomer();
            logger.debug("Cliente autenticado: " + cliente.getUsuario().getUsername());

            Optional<Cliente> optionalCliente = matrimonioService.findClientById(cliente.getIdCliente());
            if (optionalCliente.isPresent()) {
                List<Solicitud> solicitudesActualizadas = matrimonioService.actualizarEstadoYObtenerSolicitudes(optionalCliente.get().getIdCliente());
                logger.info("Solicitudes actualizadas exitosamente para el cliente con ID: " + cliente.getIdCliente());

                model.addAttribute("solicitudes", solicitudesActualizadas);
                return "ver-solicitudes";
            } else {
                logger.warn("No se encontró el cliente con ID: " + cliente.getIdCliente());
                flash.addFlashAttribute("error", "No se encontró el cliente en el sistema.");
            }
        } catch (Exception e) {
            logger.error("Error al cargar las solicitudes del cliente.", e);
            flash.addFlashAttribute("error", "Ocurrió un error al cargar las solicitudes.");
        }

        return "redirect:/home";
    }

    @PostMapping("/cliente/solicitudes/aceptar/{id}")
    public String aceptarSolicitud(@PathVariable Integer id, Model model) {
        logger.info("El cliente está aceptando la solicitud con ID: " + id);

        try {
            Solicitud solicitud = matrimonioService.findSolicitudById(id);
            logger.debug("Solicitud encontrada: " + solicitud.getIdSolicitud() + ", estado actual: " + solicitud.getEstado());

            solicitud.setEstado(Solicitud.EstadoSolicitud.CERRADO);
            matrimonioService.saveSolicitud(solicitud);
            logger.info("Solicitud aceptada y actualizada exitosamente.");
        } catch (Exception e) {
            logger.error("Error al aceptar la solicitud con ID: " + id, e);
            return "redirect:/cliente/ver-solicitudes?error=true";
        }

        return "redirect:/cliente/ver-solicitudes";
    }

    // Acción para rechazar presupuesto
    @PostMapping("/cliente/solicitudes/rechazar/{id}")
    public String rechazarSolicitud(@PathVariable Integer id, Model model) {
        logger.info("El cliente está rechazando la solicitud con ID: " + id);

        try {
            Solicitud solicitud = matrimonioService.findSolicitudById(id);
            logger.debug("Solicitud encontrada: " + solicitud.getIdSolicitud() + ", estado actual: " + solicitud.getEstado());

            solicitud.setEstado(Solicitud.EstadoSolicitud.CANCELADO);
            matrimonioService.saveSolicitud(solicitud);
            logger.info("Solicitud rechazada y actualizada exitosamente.");

            List<Solicitud> solicitudes = matrimonioService.obtenerSolicitudesPorCliente(solicitud.getCliente().getIdCliente());
            model.addAttribute("solicitudes", solicitudes);
        } catch (Exception e) {
            logger.error("Error al rechazar la solicitud con ID: " + id, e);
            return "redirect:/cliente/ver-solicitudes?error=true";
        }

        return "redirect:/cliente/ver-solicitudes";
    }
}