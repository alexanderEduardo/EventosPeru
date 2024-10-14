package com.alex.spring.security.demo.services;

import com.alex.spring.security.demo.controllers.dto.CreateProviderRequest;
import com.alex.spring.security.demo.controllers.dto.EditClientRequest;
import com.alex.spring.security.demo.controllers.dto.EditProviderRequest;
import com.alex.spring.security.demo.persistence.entity.*;

import java.util.List;
import java.util.Optional;

public interface IUserService {
     List<UserEntity> findAllUsers();
     Optional<UserEntity> findUserByEmail(String email);
    List<UserEntity> saveAllUsers(List<UserEntity> users);
     UserEntity saveUser(UserEntity userEntity);
    List<RoleEntity> findRolesByRoleEnumList(List<RoleEnum> roleEnums);
    void deleteClientById(Integer id);
     List<Cliente> findAllClients();
    Optional<Cliente> findClient(Integer id);
    void updateClientAndUser(EditClientRequest clientRequest);
    void createUserAndClient(UserEntity user, Cliente cliente);
    List<Proveedor> findAllProviders();
    Cliente saveCliente(Cliente cliente);
    Proveedor saveProveedor(Proveedor proveedor);
    void deleteProviderById(Integer id);
    List<Especialidad> getSpecialties();
    Optional<Proveedor> findProvider(Integer id);
    void createUserAndProvider(UserEntity user, Proveedor proveedor);
    void updateProviderAndUser(EditProviderRequest providerRequest);
    void registerUserAsProvider(CreateProviderRequest createProviderRequest);
}
