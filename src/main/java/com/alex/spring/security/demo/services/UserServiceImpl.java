package com.alex.spring.security.demo.services;

import com.alex.spring.security.demo.controllers.dto.CreateProviderRequest;
import com.alex.spring.security.demo.controllers.dto.EditClientRequest;
import com.alex.spring.security.demo.controllers.dto.EditProviderRequest;
import com.alex.spring.security.demo.persistence.entity.*;
import com.alex.spring.security.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements IUserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    ProveedorRepository proveedorRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EspecialidadRepository especialidadRepository;

    @Transactional(readOnly = true)
    @Override
    public List<UserEntity> findAllUsers() {
        return (List<UserEntity>) userRepository.findAll();
    }

    @Override
    public List<Cliente> findAllClients() {
        return (List<Cliente>) clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<UserEntity> findUserByEmail(String email) {
        return userRepository.findUserEntityByEmail(email);
    }

    @Transactional
    @Override
    public UserEntity saveUser(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    @Transactional
    @Override
    public void createUserAndClient(UserEntity user, Cliente cliente) {
        UserEntity savedUser = userRepository.save(user);
        cliente.setUsuario(savedUser);
        clienteRepository.save(cliente);
    }



    @Transactional
    @Override
    public void createUserAndProvider(UserEntity user, Proveedor proveedor) {
        UserEntity savedUser = userRepository.save(user);
        proveedor.setUsuario(savedUser);
        proveedorRepository.save(proveedor);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoleEntity> findRolesByRoleEnumList(List<RoleEnum> roleEnums) {
        return roleRepository.findRoleEntitiesByRoleEnumIn(roleEnums);
    }

    @Override
    @Transactional
    public List<UserEntity> saveAllUsers(List<UserEntity> users) {
        Iterable<UserEntity> userEntities = userRepository.saveAll(users);
        return (List<UserEntity>) userEntities;
    }

    @Override
    @Transactional
    public Cliente saveCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Override
    public Proveedor saveProveedor(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    @Override
    @Transactional
    public void updateClientAndUser(EditClientRequest clientRequest) {

        Cliente cliente = clienteRepository.findById(clientRequest.id())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + clientRequest.id()));

        UserEntity usuario = cliente.getUsuario();

        cliente.setApellido(clientRequest.lastname());
        cliente.setDireccion(clientRequest.address());
        cliente.setTelefono(clientRequest.phone());

        // 4. Actualizar la información del usuario con los datos de clientRequest
        usuario.setUsername(clientRequest.name());
        usuario.setEmail(clientRequest.email());
        usuario.setIsEnabled(clientRequest.isEnabled());

        // Actualización de la contraseña si es necesaria
        if (!usuario.getPassword().equals(clientRequest.password())) {
            // Aquí puedes usar un PasswordEncoder para encriptar la nueva contraseña
            String encodedPassword = passwordEncoder.encode(clientRequest.password());
            usuario.setPassword(encodedPassword);
        }

        // 5. Guardar el usuario actualizado
        userRepository.save(usuario);  // Guarda primero el usuario para asegurar que esté sincronizado

        // 6. Guardar el cliente actualizado
        clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public void updateProviderAndUser(EditProviderRequest providerRequest) {

        Proveedor proveedor = proveedorRepository.findById(providerRequest.idProveedor())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + providerRequest.idProveedor()));

        UserEntity usuario = proveedor.getUsuario();
        proveedor.setCalificacion(providerRequest.calificacion());
        proveedor.setEspecialidad(providerRequest.especialidad());

        usuario.setUsername(providerRequest.name());
        usuario.setEmail(providerRequest.email());
        usuario.setIsEnabled(providerRequest.isEnabled());

        if (!usuario.getPassword().equals(providerRequest.password())) {
            String encodedPassword = passwordEncoder.encode(providerRequest.password());
            usuario.setPassword(encodedPassword);
        }

        userRepository.save(usuario);
        proveedorRepository.save(proveedor);
    }

    @Transactional
    @Override
    public void registerUserAsProvider(CreateProviderRequest createProviderRequest) {
        //crear un registro en la tabla usuario y la tabla clientes
        String name = createProviderRequest.name();
        String email = createProviderRequest.email();
        String password = createProviderRequest.password();
        Float rating = createProviderRequest.calificacion();
        Especialidad especialidad = createProviderRequest.especialidad();
        Boolean accountNoLocked = createProviderRequest.isAccountNoLocked();
        Boolean enabled = createProviderRequest.isEnabled();
        Boolean accountNoExpired = createProviderRequest.isAccountNoExpired();
        Boolean credentialNoExpired = createProviderRequest.isCredentialNoExpired();

        List<RoleEnum> rolesInit = List.of(RoleEnum.USER, RoleEnum.PROVEEDOR);
        List<RoleEntity> rolesList = findRolesByRoleEnumList(rolesInit);
        Set<RoleEntity> rolesDb = new HashSet<>(rolesList);
        String encodedPassword = passwordEncoder.encode(password);

        UserEntity userEntity = UserEntity.builder()
                .username(name)
                .password(encodedPassword)
                .roleEntitySet(rolesDb)
                .isEnabled(enabled)
                .accountNoExpired(credentialNoExpired)
                .accountNoLocked(accountNoLocked)
                .credentialNoExpired(accountNoExpired)
                .email(email)
                .build();

        Proveedor proveedor = Proveedor.builder()
                .calificacion(rating)
                .especialidad(especialidad)
                .build();

        createUserAndProvider(userEntity, proveedor);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> findClient(Integer id) {
        return clienteRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteClientById(Integer id) {
        clienteRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void deleteProviderById(Integer id) {
        proveedorRepository.deleteById(id);
    }

    @Override
    public List<Proveedor> findAllProviders() {
        return (List<Proveedor>) proveedorRepository.findAll();
    }
    @Override
    public List<Especialidad> getSpecialties() {
        return (List<Especialidad>) especialidadRepository.findAll();
    }
    @Override
    public Optional<Proveedor> findProvider(Integer id) {
        return proveedorRepository.findById(id);
    }
    public void createUserAsClient(UserEntity user, Cliente cliente) {
        UserEntity savedUser = userRepository.save(user);
        cliente.setUsuario(savedUser);
        clienteRepository.save(cliente);
    }
}

