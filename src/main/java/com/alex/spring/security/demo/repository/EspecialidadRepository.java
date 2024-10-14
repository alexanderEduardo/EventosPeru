package com.alex.spring.security.demo.repository;

import com.alex.spring.security.demo.persistence.entity.Especialidad;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecialidadRepository extends CrudRepository<Especialidad, Integer> {
}