package com.alex.spring.security.demo.services;

import com.alex.spring.security.demo.controllers.dto.AuthLoginRequest;
import com.alex.spring.security.demo.controllers.dto.AuthRegisterRequest;
import com.alex.spring.security.demo.controllers.dto.AuthRegisterResponse;
import com.alex.spring.security.demo.controllers.dto.AuthResponse;
import com.alex.spring.security.demo.persistence.entity.Cliente;
import com.alex.spring.security.demo.persistence.entity.RoleEntity;
import com.alex.spring.security.demo.persistence.entity.RoleEnum;
import com.alex.spring.security.demo.persistence.entity.UserEntity;
import com.alex.spring.security.demo.persistence.entity.utils.UserCustomDetails;
import com.alex.spring.security.demo.repository.RoleRepository;
import com.alex.spring.security.demo.repository.UserRepository;
import com.alex.spring.security.demo.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Primary
public class UserDetailsServiceImpl implements UserDetailsService {

    final UserRepository userRepository;
    final RoleRepository roleRepository;
    final JwtUtil jwtUtil;
    final PasswordEncoder passwordEncoder;
    final IUserService userService;

    public UserDetailsServiceImpl(UserRepository userRepository, RoleRepository roleRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder,IUserService userService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userService.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario con el correo " + email + " no existe"));

        List<SimpleGrantedAuthority> authorities = userEntity.getRoleEntitySet().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleEnum().name()))
                .collect(Collectors.toList());

        return new User(userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getIsEnabled(),
                userEntity.getAccountNoExpired(),
                userEntity.getCredentialNoExpired(),
                userEntity.getAccountNoLocked(),
                authorities);
    }

    public AuthResponse loginUser(AuthLoginRequest authLoginRequest, HttpServletRequest request, AuthenticationManager authenticationManager){
        String email = authLoginRequest.email();
        String password = authLoginRequest.password();
        UserCustomDetails userDetails = new UserCustomDetails(email);

        //Authentication authentication = this.authenticate(username, password);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        // Establecer los detalles personalizados en la autenticación
        ((UsernamePasswordAuthenticationToken) authentication).setDetails(userDetails);
        securityContext.setAuthentication(authentication);
        // También puedes asociar la sesión con el contexto de seguridad
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

        String accessToken = jwtUtil.createToken(authentication);
        AuthResponse authResponse = new AuthResponse(email, "User logged successfully", accessToken, true);
        return authResponse;
    }

    public AuthRegisterResponse registerUserAsClient(AuthRegisterRequest userRequest){
        //crear un registro en la tabla usuario y la tabla clientes
        String name = userRequest.name();
        String address = userRequest.address();
        String email = userRequest.email();
        String password = userRequest.password();
        String lastname = userRequest.lastname();
        String phone = userRequest.phone();
        Boolean accountNoLocked = userRequest.isAccountNoLocked();
        Boolean enabled = userRequest.isEnabled();
        Boolean accountNoExpired = userRequest.isAccountNoExpired();
        Boolean credentialNoExpired = userRequest.isCredentialNoExpired();

        List<RoleEnum> rolesInit = List.of(RoleEnum.USER, RoleEnum.CLIENTE);
        List<RoleEntity> rolesList = userService.findRolesByRoleEnumList(rolesInit);
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

        Cliente cliente = Cliente.builder()
                .telefono(phone)
                .direccion(address)
                .apellido(lastname)
                .build();

        userService.createUserAndClient(userEntity, cliente);

        return new AuthRegisterResponse(name,"El usuario de tipo cliente ha sido creado exitosamente",true);
    }

    private Authentication authenticate(String username, String password) {
        UserDetails userDetails = this.loadUserByUsername(username);
        if (userDetails == null) {
            throw new BadCredentialsException(String.format("Invalid username or password"));
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Incorrect Password");
        }
        new UsernamePasswordAuthenticationToken(username, password);
        return new UsernamePasswordAuthenticationToken(username, password, userDetails.getAuthorities());
    }
}