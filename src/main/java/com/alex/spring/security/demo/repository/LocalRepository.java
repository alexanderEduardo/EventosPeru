package com.alex.spring.security.demo.repository;

import com.alex.spring.security.demo.persistence.entity.Local;
import org.springframework.data.repository.CrudRepository;

public interface LocalRepository extends CrudRepository<Local,Integer> {
}
