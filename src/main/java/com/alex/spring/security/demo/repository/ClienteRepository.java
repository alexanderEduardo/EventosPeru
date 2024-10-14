package com.alex.spring.security.demo.repository;

import com.alex.spring.security.demo.persistence.entity.Cliente;
import org.springframework.data.repository.CrudRepository;

public interface ClienteRepository extends CrudRepository<Cliente,Integer> {
}
