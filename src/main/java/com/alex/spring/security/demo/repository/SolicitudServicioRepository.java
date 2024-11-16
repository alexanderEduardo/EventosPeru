package com.alex.spring.security.demo.repository;

import com.alex.spring.security.demo.persistence.entity.SolicitudServicio;
import org.springframework.data.repository.CrudRepository;

public interface SolicitudServicioRepository extends CrudRepository<SolicitudServicio,Integer> {
}
