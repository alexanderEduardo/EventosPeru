package com.alex.spring.security.demo.services;

import com.alex.spring.security.demo.controllers.dto.CreateProviderRequest;
import com.alex.spring.security.demo.controllers.dto.EditClientRequest;
import com.alex.spring.security.demo.controllers.dto.EditProviderRequest;
import com.alex.spring.security.demo.persistence.entity.*;

import java.util.List;
import java.util.Optional;

public interface IUserService {
     Optional<UserEntity> findUserByEmail(String email);
    List<RoleEntity> findRolesByRoleEnumList(List<RoleEnum> roleEnums);
    void deleteClientById(Integer id);
     List<Cliente> findAllClients();
    Optional<Cliente> findClient(Integer id);
    void updateClientAndUser(EditClientRequest clientRequest);
    void createUserAndClient(UserEntity user, Cliente cliente);
    List<Proveedor> findAllProviders();
    void deleteProviderById(Integer id);
    List<Especialidad> getSpecialties();
    Optional<Proveedor> findProvider(Integer id);
    void createUserAndProvider(UserEntity user, Proveedor proveedor);
    void updateProviderAndUser(EditProviderRequest providerRequest);
    void registerUserAsProvider(CreateProviderRequest createProviderRequest);
    Cliente buscarClientePorEmail(String email);
}
