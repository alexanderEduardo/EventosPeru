package com.alex.spring.security.demo.test;

import com.alex.spring.security.demo.controllers.dto.EditClientRequest;
import com.alex.spring.security.demo.persistence.entity.*;
import com.alex.spring.security.demo.repository.*;
import com.alex.spring.security.demo.services.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProveedorRepository proveedorRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    private UserEntity userEntity;
    private Cliente cliente;
    private Proveedor proveedor;
    private List<RoleEntity> roles;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        userEntity = UserEntity.builder()
                .id(1L)
                .username("Juan")
                .email("juan@correo.com")
                .password("encodedPassword")
                .isEnabled(true)
                .build();

        cliente = Cliente.builder()
                .idCliente(1)
                .usuario(userEntity)
                .direccion("Calle A")
                .telefono("123456789")
                .apellido("Perez")
                .build();

        proveedor = Proveedor.builder()
                .idProveedor(1)
                .usuario(userEntity)
                .especialidad(new Especialidad(1,"Especialidad 1"))
                .calificacion(4.5f)
                .build();

        roles = List.of(new RoleEntity(1L, RoleEnum.USER), new RoleEntity(2L, RoleEnum.PROVEEDOR));
    }

    @Test
    public void testFindAllClients() {
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));

        List<Cliente> result = userService.findAllClients();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Perez", result.get(0).getApellido());

        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    public void testFindUserByEmail() {

        when(userRepository.findUserEntityByEmail("juan@correo.com")).thenReturn(Optional.of(userEntity));

        Optional<UserEntity> result = userService.findUserByEmail("juan@correo.com");

        assertTrue(result.isPresent());
        assertEquals("Juan", result.get().getUsername());

        verify(userRepository, times(1)).findUserEntityByEmail("juan@correo.com");
    }

    @Test
    public void testCreateUserAndClient() {
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        userService.createUserAndClient(userEntity, cliente);

        verify(userRepository, times(1)).save(userEntity);
        verify(clienteRepository, times(1)).save(cliente);

        assertEquals(userEntity, cliente.getUsuario());
    }

    @Test
    public void testCreateUserAndProvider() {

        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        userService.createUserAndProvider(userEntity, proveedor);

        verify(userRepository, times(1)).save(userEntity);
        verify(proveedorRepository, times(1)).save(proveedor);

        assertEquals(userEntity, proveedor.getUsuario());
    }

    @Test
    public void testFindRolesByRoleEnumList() {

        when(roleRepository.findRoleEntitiesByRoleEnumIn(anyList())).thenReturn(roles);

        List<RoleEntity> result = userService.findRolesByRoleEnumList(List.of(RoleEnum.USER, RoleEnum.PROVEEDOR));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(RoleEnum.USER, result.get(0).getRoleEnum());

        verify(roleRepository, times(1)).findRoleEntitiesByRoleEnumIn(anyList());
    }

    @Test
    public void testUpdateClientAndUser() {

        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(passwordEncoder.encode("12345")).thenReturn("encoded12345");

        EditClientRequest request = new EditClientRequest(1, "Nuevo Nombre",
                "Nuevo Apellido", "nuevo@correo.com", "12345",
                "Calle B","987654321",true,true,true,
                true);

        userService.updateClientAndUser(request);

        assertEquals("Nuevo Nombre", cliente.getUsuario().getUsername());
        assertEquals("Calle B", cliente.getDireccion());
        assertEquals("encoded12345", cliente.getUsuario().getPassword());

        verify(clienteRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }
}
