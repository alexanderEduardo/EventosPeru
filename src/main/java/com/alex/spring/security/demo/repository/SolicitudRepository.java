package com.alex.spring.security.demo.repository;

import com.alex.spring.security.demo.persistence.entity.Solicitud;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepository extends CrudRepository<Solicitud,Integer> {
    List<Solicitud> findByClienteIdCliente(Integer idCliente);

    @Modifying
    @Query("UPDATE Solicitud s SET s.estado = 'ESPERANDO_RESPUESTA_DEL_CLIENTE' WHERE s.cliente.idCliente = :idCliente AND s.estado = 'PRESUPUESTO_ENVIADO'")
    void updateEstadoSolicitudByClienteId(@Param("idCliente") Integer idCliente);
}