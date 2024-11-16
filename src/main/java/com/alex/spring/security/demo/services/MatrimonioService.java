package com.alex.spring.security.demo.services;

import com.alex.spring.security.demo.persistence.entity.Cliente;
import com.alex.spring.security.demo.persistence.entity.Local;
import com.alex.spring.security.demo.persistence.entity.LocalServicio;
import com.alex.spring.security.demo.persistence.entity.Solicitud;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MatrimonioService {
    List<Local> getLocales();
    Optional<Local> findLocalById(Integer id);
    List<LocalServicio> findByLocalService(Local local);
    Solicitud crearSolicitud(Cliente cliente, Local local, LocalDate fechaEvento, Integer cantidadInvitados, BigDecimal presupuestoFinal,String correo);
    void guardarServiciosParaSolicitud(Solicitud solicitud, List<Integer> serviciosSeleccionados);
    Solicitud findSolicitudById(Integer id);
    void saveSolicitud(Solicitud solicitud);

    List<Solicitud> obtenerSolicitudes();

}
