package com.alex.spring.security.demo.controllers;

import com.alex.spring.security.demo.controllers.dto.ResponseSolicitudPresupuesto;
import com.alex.spring.security.demo.controllers.dto.SolicitudFormDTO;
import com.alex.spring.security.demo.persistence.entity.*;
import com.alex.spring.security.demo.persistence.entity.utils.UserCustomDetails;
import com.alex.spring.security.demo.services.EmailService;
import com.alex.spring.security.demo.services.IUserService;
import com.alex.spring.security.demo.services.MatrimonioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class MatrimonioController {

    @Autowired
    MatrimonioService matrimonioService;

    @Autowired
    IUserService userService;

    @Autowired
    private EmailService emailService;
    @GetMapping(value = {"/public/locales"})
    public String verLocales(Model model){
        List<Local> locales = matrimonioService.getLocales();
        model.addAttribute("locales",locales);
        return "locales";
    }

    @GetMapping("matrimonio/solicitar-presupuesto/{idLocal}")
    public String solicitarPresupuesto(@PathVariable Integer idLocal, Model model, RedirectAttributes flash) {
        // Lógica para procesar la solicitud de presupuesto para el local con el ID proporcionado.
        Optional<Local> localOptional = matrimonioService.findLocalById(idLocal);
        if(localOptional.isPresent()){
            Local local = localOptional.get();
            List<LocalServicio> serviciosDisponibles = matrimonioService.findByLocalService(local);
            model.addAttribute("local", local);
            model.addAttribute("serviciosDisponibles", serviciosDisponibles);

            return "formularioPresupuesto"; // Página donde se completa la solicitud
        }
        flash.addFlashAttribute("error", "Error en el sistema.");
        return "redirect:/home";
    }

    @PostMapping("matrimonio/procesar-solicitud-presupuesto")
    @ResponseBody
    public ResponseEntity<ResponseSolicitudPresupuesto> procesarSolicitudPresupuesto(@RequestBody SolicitudFormDTO solicitudForm) {
        Integer cantidadInvitados = solicitudForm.getCantidadInvitados();
        String comentarios = solicitudForm.getComentarios();
        String email = solicitudForm.getEmail();
        String nombre = solicitudForm.getNombre();
        Integer localId = solicitudForm.getLocalId();
        String telefono = solicitudForm.getTelefono();
        LocalDate fechaEvento = solicitudForm.getFechaEvento();
        List<Integer> serviciosSeleccionados = solicitudForm.getServiciosSeleccionados();

        String emailFromCurrentUser = getEmailFromCurrentUser();
        Cliente cliente = userService.buscarClientePorEmail(emailFromCurrentUser);
        if(cliente == null){
            throw new RuntimeException("el cliente no fue encontrado");
        }

        // Buscar el local seleccionado
        Local local = matrimonioService.findLocalById(localId)
                .orElseThrow(() -> new RuntimeException("Local no encontrado"));
        BigDecimal precioBaseLocal = local.getPrecioBase();
        BigDecimal presupuestoFinalInicial = BigDecimal.ZERO;
        presupuestoFinalInicial = presupuestoFinalInicial.add(precioBaseLocal);
        for (Integer idLocalServicio : serviciosSeleccionados) {
            LocalServicio localServicio = matrimonioService.findByLocalService(local).stream()
                    .filter(ls -> ls.getIdLocalServicio().equals(idLocalServicio))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
            presupuestoFinalInicial = presupuestoFinalInicial.add(localServicio.getPrecio());
        }

        // Crear y guardar la solicitud
        Solicitud solicitud = matrimonioService.crearSolicitud(cliente, local, solicitudForm.getFechaEvento(), cantidadInvitados, presupuestoFinalInicial,email);

        // Guardar los servicios seleccionados para la solicitud
        matrimonioService.guardarServiciosParaSolicitud(solicitud, serviciosSeleccionados);

        // Enviar correos de notificación
        // Enviar correo al administrador
        String comentariosCliente = "Estos son los comentarios del cliente: " + comentarios;
        emailService.enviarCorreo("admin.eventos_peru@yopmail.com", "Nueva solicitud de evento creada",
                "Se ha creado una nueva solicitud de evento para el cliente "+nombre+" que tiene el correo "+email+
                        " y el telefono "+telefono+ "\n\n" + comentariosCliente);

        // Enviar correo al cliente
        emailService.enviarCorreo(solicitudForm.getEmail(), "Confirmación de solicitud de presupuesto",
                "Gracias por solicitar un presupuesto. Pronto recibirá una respuesta.");

        return ResponseEntity.ok(new ResponseSolicitudPresupuesto(nombre,
                email,
                fechaEvento,
                presupuestoFinalInicial,
                "Muchas gracias por crear la solicitud. Pronto recibira un correo con la confirmacion de su presupuesto"));
    }

    private static String getEmailFromCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserCustomDetails details = (UserCustomDetails) authentication.getDetails();
        String emailFromCurrentUser = details.getEmail();
        return emailFromCurrentUser;
    }

    @PostMapping("/admin/actualizar-presupuesto/{id}")
    public String actualizarPresupuesto(@PathVariable Integer id, @RequestParam BigDecimal presupuestoFinal) {
        Solicitud solicitud = matrimonioService.findSolicitudById(id);
        solicitud.setPresupuestoFinal(presupuestoFinal);
        solicitud.setEstado(Solicitud.EstadoSolicitud.PRESUPUESTO_ENVIADO);
        matrimonioService.saveSolicitud(solicitud);
        emailService.enviarCorreo(solicitud.getCorreo(), "Confirmación de presupuesto",
                "Gracias por solicitar agendar un evento para su matrimonio. Su presupuesto final es "+presupuestoFinal);
        return "redirect:/admin/solicitudes";
    }

    // Página para revisar una solicitud específica
    @GetMapping("/admin/revisar-solicitud/{id}")
    public String revisarSolicitud(@PathVariable Integer id, Model model) {
        Solicitud solicitud = matrimonioService.findSolicitudById(id);
        model.addAttribute("solicitud", solicitud);
        return "revisarSolicitud";
    }

}
