package com.alex.spring.security.demo.repository;

import com.alex.spring.security.demo.persistence.entity.Proveedor;
import org.springframework.data.repository.CrudRepository;

public interface ProveedorRepository extends CrudRepository<Proveedor,Integer> {
}
