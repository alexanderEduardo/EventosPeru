package com.alex.spring.security.demo.repository;

import com.alex.spring.security.demo.persistence.entity.Solicitud;
import org.springframework.data.repository.CrudRepository;

public interface SolicitudRepository extends CrudRepository<Solicitud,Integer> {
}
