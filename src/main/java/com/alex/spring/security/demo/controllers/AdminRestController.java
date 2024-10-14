package com.alex.spring.security.demo.controllers;

import com.alex.spring.security.demo.controllers.dto.*;
import com.alex.spring.security.demo.persistence.entity.Cliente;
import com.alex.spring.security.demo.persistence.entity.Proveedor;
import com.alex.spring.security.demo.services.IUserService;
import com.alex.spring.security.demo.services.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AdminRestController {

    @Autowired
    IUserService userService;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @GetMapping(value = "/clientes")
    public ResponseEntity<List<GetClientResponse>> getClients(){
        List<Cliente> clients = userService.findAllClients();

        List<GetClientResponse> clientResponses = clients.stream()
                .map(this::mapToGetClientResponse)
                .collect(Collectors.toList());

        return new ResponseEntity<>(clientResponses, HttpStatus.OK);
    }

    @PostMapping(value = "/clientes/create")
    public ResponseEntity<AuthRegisterResponse> saveClient(@RequestBody @Valid AuthRegisterRequest userRequest){
        userDetailsService.registerUserAsClient(userRequest);
        return new ResponseEntity<>(new AuthRegisterResponse(
            userRequest.name(),
            "se creo el usuario correctamente",
            true), HttpStatus.OK);
    }

    @PutMapping(value = "/clientes/update")
    public ResponseEntity<AuthRegisterResponse> editClient(@RequestBody @Valid EditClientRequest userRequest){
        userService.updateClientAndUser(userRequest);
        return new ResponseEntity<>(new AuthRegisterResponse(
            userRequest.name(),
            "se actualizo el usuario correctamente",
            true), HttpStatus.OK);
    }

    @GetMapping(value = "/clientes/{id}")
    public ResponseEntity<GetClientResponse> getClient(@PathVariable Integer id){
        Optional<Cliente> clientOptional = userService.findClient(id);
        if(clientOptional.isEmpty()){
            return new ResponseEntity<>(new GetClientResponse(),HttpStatus.NOT_FOUND);
        }
        Cliente cliente = clientOptional.get();
        return new ResponseEntity<>(new GetClientResponse(
                cliente.getIdCliente(),
                cliente.getUsuario().getUsername(),
                cliente.getApellido(),
                cliente.getUsuario().getEmail(),
                cliente.getDireccion(),
                cliente.getTelefono(),
                cliente.getUsuario().getIsEnabled(),
                cliente.getUsuario().getCredentialNoExpired(),
                cliente.getUsuario().getAccountNoLocked(),
                cliente.getUsuario().getAccountNoExpired()),HttpStatus.OK);
    }

    @DeleteMapping(value = "/clientes/delete/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable Integer id){
        userService.deleteClientById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @PostMapping(value = "/proveedores/create")
    public ResponseEntity<AuthRegisterResponse> saveProvider(@RequestBody @Valid CreateProviderRequest createProviderRequest){
        userService.registerUserAsProvider(createProviderRequest);
        return new ResponseEntity<>(new AuthRegisterResponse(createProviderRequest.name(),"se creo el proveedor correctamente",true), HttpStatus.OK);
    }

    @PutMapping(value = "/proveedores/update")
    public ResponseEntity<AuthRegisterResponse> editProvider(@RequestBody @Valid EditProviderRequest providerRequest){
        userService.updateProviderAndUser(providerRequest);
        return new ResponseEntity<>(new AuthRegisterResponse(providerRequest.name(),"se actualizo el proveedor correctamente",true), HttpStatus.OK);
    }

    @DeleteMapping(value = "/proveedores/delete/{id}")
    public ResponseEntity<?> deleteProvider(@PathVariable Integer id){
        userService.deleteProviderById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/proveedores/{id}")
    public ResponseEntity<GetProviderResponse> getProvider(@PathVariable Integer id){
        Optional<Proveedor> proveedorOptional = userService.findProvider(id);
        if(proveedorOptional.isEmpty()){
            return new ResponseEntity<>(new GetProviderResponse(),HttpStatus.NOT_FOUND);
        }
        Proveedor proveedor = proveedorOptional.get();
        return new ResponseEntity<>(new GetProviderResponse(
                proveedor.getIdProveedor(),
                proveedor.getUsuario().getUsername(),
                proveedor.getUsuario().getEmail(),
                proveedor.getEspecialidad(),
                proveedor.getCalificacion(),
                proveedor.getUsuario().getIsEnabled(),
                proveedor.getUsuario().getCredentialNoExpired(),
                proveedor.getUsuario().getAccountNoLocked(),
                proveedor.getUsuario().getAccountNoExpired()),HttpStatus.OK);
    }

    @GetMapping(value = "/proveedores")
    public ResponseEntity<List<GetProviderResponse>> getProviders(){
        List<Proveedor> providers = userService.findAllProviders();

        List<GetProviderResponse> providerResponses = providers.stream()
                .map(this::mapToGetProviderResponse)
                .collect(Collectors.toList());

        return new ResponseEntity<>(providerResponses, HttpStatus.OK);
    }

    private GetProviderResponse mapToGetProviderResponse(Proveedor proveedor) {
        return GetProviderResponse.builder()
                .id(proveedor.getIdProveedor())
                .name(proveedor.getUsuario().getUsername())
                .email(proveedor.getUsuario().getEmail())
                .especialidad(proveedor.getEspecialidad())
                .calificacion(proveedor.getCalificacion())
                .isEnabled(proveedor.getUsuario().getIsEnabled())
                .isCredentialNoExpired(proveedor.getUsuario().getCredentialNoExpired())
                .isAccountNoLocked(proveedor.getUsuario().getAccountNoLocked())
                .isAccountNoExpired(proveedor.getUsuario().getAccountNoExpired())
                .build();
    }

    private GetClientResponse mapToGetClientResponse(Cliente cliente) {
        return GetClientResponse.builder()
                .id(cliente.getIdCliente())
                .name(cliente.getUsuario().getUsername())
                .lastname(cliente.getApellido())
                .email(cliente.getUsuario().getEmail())
                .address(cliente.getDireccion())
                .phone(cliente.getTelefono())
                .isEnabled(cliente.getUsuario().getIsEnabled())
                .isCredentialNoExpired(cliente.getUsuario().getCredentialNoExpired())
                .isAccountNoLocked(cliente.getUsuario().getAccountNoLocked())
                .isAccountNoExpired(cliente.getUsuario().getAccountNoExpired())
                .build();
    }



}
