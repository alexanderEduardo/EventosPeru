package com.alex.spring.security.demo.services;

import com.alex.spring.security.demo.persistence.entity.*;
import com.alex.spring.security.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MatrimonioServiceImpl implements MatrimonioService {

    @Autowired
    LocalRepository localRepository;

    @Autowired
    LocalServicioRepository localServicioRepository;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private SolicitudServicioRepository solicitudServicioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Local> getLocales() {
        return (List<Local>) localRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Local> findLocalById(Integer id) {
        return localRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalServicio> findByLocalService(Local local) {
        return localServicioRepository.findByLocal(local);
    }

    // Método para crear y guardar una solicitud
    @Override
    @Transactional
    public Solicitud crearSolicitud(Cliente cliente, Local local, LocalDate fechaEvento, Integer cantidadInvitados, BigDecimal presupuestoFinalInicial,String correo) {
        Solicitud solicitud = new Solicitud();
        solicitud.setCliente(cliente);
        solicitud.setLocal(local);
        solicitud.setFechaEvento(fechaEvento);
        solicitud.setCantidadInvitados(cantidadInvitados);
        solicitud.setPresupuestoFinal(presupuestoFinalInicial);
        solicitud.setEstado(Solicitud.EstadoSolicitud.RECIBIDO);
        solicitud.setFechaCreacion(LocalDateTime.now());
        solicitud.setFechaActualizacion(LocalDateTime.now());
        solicitud.setCorreo(correo);
        return solicitudRepository.save(solicitud); // Guarda y devuelve la solicitud creada
    }


    // Método para asociar los servicios seleccionados a una solicitud
    @Override
    @Transactional
    public void guardarServiciosParaSolicitud(Solicitud solicitud, List<Integer> serviciosSeleccionados) {
        for (Integer idLocalServicio : serviciosSeleccionados) {
            LocalServicio localServicio = localServicioRepository.findById(idLocalServicio)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
            SolicitudServicio solicitudServicio = new SolicitudServicio();
            solicitudServicio.setSolicitud(solicitud);
            solicitudServicio.setLocalServicio(localServicio);
            solicitudServicio.setPrecioEstimado(localServicio.getPrecio());

            solicitudServicioRepository.save(solicitudServicio); // Guarda cada servicio asociado
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Solicitud findSolicitudById(Integer id) {
        return solicitudRepository.findById(id).orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
    }

    @Transactional
    @Override
    public void saveSolicitud(Solicitud solicitud) {
        solicitudRepository.save(solicitud);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Solicitud> obtenerSolicitudes() {
        return (List<Solicitud>) solicitudRepository.findAll();
    }

    @Transactional
    @Override
    public Local saveLocal(Local local) {
        return localRepository.save(local);
    }

    @Transactional
    @Override
    public void deleteLocalById(Integer id) {
        localRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Cliente> findClientById(Integer id) {
        return clienteRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Solicitud> obtenerSolicitudesPorCliente(Integer idCliente) {
        return solicitudRepository.findByClienteIdCliente(idCliente);
    }

    @Transactional
    @Override
    public List<Solicitud> actualizarEstadoYObtenerSolicitudes(Integer idCliente) {
        // Actualizar las solicitudes con estado PRESUPUESTO_ENVIADO a ESPERANDO_RESPUESTA_DEL_CLIENTE
        solicitudRepository.updateEstadoSolicitudByClienteId(idCliente);

        // Obtener las solicitudes actualizadas del cliente
        return solicitudRepository.findByClienteIdCliente(idCliente);
    }

    @Override
    public List<Proveedor> getProveedores() {
        return (List<Proveedor>) proveedorRepository.findAll();
    }

}
