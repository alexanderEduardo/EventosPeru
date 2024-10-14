package com.alex.spring.security.demo;

import ch.qos.logback.core.net.server.Client;
import com.alex.spring.security.demo.persistence.entity.*;
import com.alex.spring.security.demo.repository.EspecialidadRepository;
import com.alex.spring.security.demo.repository.UserRepository;
import com.alex.spring.security.demo.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class SpringSecurityDemoApplication {

    public SpringSecurityDemoApplication(IUserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityDemoApplication.class, args);
    }

    final IUserService userService;
    final PasswordEncoder passwordEncoder;

    @Autowired
    EspecialidadRepository especialidadRepository;

    @Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {

            RoleEntity roleAdmin = RoleEntity.builder()
                    .roleEnum(RoleEnum.ADMIN)
                    .build();
            RoleEntity roleProveedor = RoleEntity.builder()
                    .roleEnum(RoleEnum.PROVEEDOR)
                    .build();
            RoleEntity roleUser = RoleEntity.builder()
                    .roleEnum(RoleEnum.USER)
                    .build();
            RoleEntity roleCliente = RoleEntity.builder()
                    .roleEnum(RoleEnum.CLIENTE)
                    .build();

            Especialidad especialidadMatrimonio = Especialidad.builder()
                    .nombre("Matrimonio")
                    .build();
            Especialidad especialidadCumpleaños = Especialidad.builder()
                    .nombre("Cumpleaños")
                    .build();
            Especialidad especialidadBabyShower = Especialidad.builder()
                    .nombre("Baby Shower")
                    .build();
            List<Especialidad> especialidades = Arrays.asList(especialidadMatrimonio, especialidadCumpleaños, especialidadBabyShower);
            especialidadRepository.saveAll(especialidades);

            String passwordEncripted = passwordEncoder.encode("1234");
            UserEntity admin = UserEntity.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("1234"))
                    .accountNoExpired(true)
                    .accountNoLocked(true)
                    .credentialNoExpired(true)
                    .isEnabled(true)
                    .roleEntitySet(Set.of(roleAdmin))
                    .email("admin@gmail.com")
                    .build();

            UserEntity alex = UserEntity.builder()
                    .username("alex")
                    .password(passwordEncoder.encode("1234"))
                    .accountNoExpired(true)
                    .accountNoLocked(true)
                    .credentialNoExpired(true)
                    .isEnabled(false)
                    .roleEntitySet(Set.of(roleCliente))
                    .email("alex@gmail.com")
                    .build();

            UserEntity pepe = UserEntity.builder()
                    .username("pepe")
                    .password(passwordEncoder.encode("1234"))
                    .accountNoExpired(true)
                    .accountNoLocked(true)
                    .credentialNoExpired(true)
                    .isEnabled(true)
                    .roleEntitySet(Set.of(roleUser))
                    .email("pepe@gmail.com")
                    .build();

            UserEntity jonas = UserEntity.builder()
                    .username("jonas")
                    .password(passwordEncoder.encode("1234"))
                    .accountNoExpired(true)
                    .accountNoLocked(true)
                    .credentialNoExpired(true)
                    .isEnabled(true)
                    .roleEntitySet(Set.of(roleProveedor))
                    .email("jonas@gmail.com")
                    .build();

            List<UserEntity> userEntities = userService.saveAllUsers(List.of(admin, alex, pepe, jonas));
            userEntities.forEach(user -> {
                if (user.getRoleEntitySet().contains(roleCliente)) {
                    Cliente cliente = Cliente.builder()
                            .apellido("Peña")
                            .direccion("Av. Lima")
                            .telefono("999299229")
                            .usuario(user)
                            .build();
                    userService.saveCliente(cliente);
                }
                if (user.getRoleEntitySet().contains(roleProveedor)) {
                    Proveedor proveedor = Proveedor.builder()
                            .calificacion(1F)
                            .especialidad(especialidadMatrimonio)
                            .usuario(user)
                            .build();
                    userService.saveProveedor(proveedor);
                }
            });
        };
    }

}
