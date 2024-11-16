package com.alex.spring.security.demo.repository;

import com.alex.spring.security.demo.persistence.entity.Local;
import com.alex.spring.security.demo.persistence.entity.LocalServicio;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LocalServicioRepository extends CrudRepository<LocalServicio, Integer> {

    // Método que obtiene todos los LocalServicio asociados a un Local específico
    List<LocalServicio> findByLocal(Local local);
}
