package com.alex.spring.security.demo.repository;

import com.alex.spring.security.demo.persistence.entity.RoleEntity;
import com.alex.spring.security.demo.persistence.entity.RoleEnum;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity,Long> {
    Optional<RoleEntity> findByRoleEnum(RoleEnum roleEnum);
    List<RoleEntity> findRoleEntitiesByRoleEnumIn(List<RoleEnum> roleEnums);
}
