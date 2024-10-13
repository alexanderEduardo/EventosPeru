package com.alex.spring.security.demo.services;

import com.alex.spring.security.demo.controllers.dto.AuthCreateUserRequest;
import com.alex.spring.security.demo.controllers.dto.AuthLoginRequest;
import com.alex.spring.security.demo.controllers.dto.AuthResponse;
import com.alex.spring.security.demo.persistence.entity.RoleEntity;
import com.alex.spring.security.demo.persistence.entity.UserEntity;
import com.alex.spring.security.demo.repository.RoleRepository;
import com.alex.spring.security.demo.repository.UserRepository;
import com.alex.spring.security.demo.util.JwtUtil;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Primary
public class UserDetailsServiceImpl implements UserDetailsService {

    final UserRepository userRepository;
    final RoleRepository roleRepository;
    final JwtUtil jwtUtil;
    final PasswordEncoder passwordEncoder;

    public UserDetailsServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findUserEntityByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario " + username + " no existe"));

        List<SimpleGrantedAuthority> authorities = getSimpleGrantedAuthorities(userEntity);

        return new User(userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.isEnabled(),
                userEntity.isAccountNoExpired(),
                userEntity.isCredentialNoExpired(),
                userEntity.isAccountNoLocked(),
                authorities);
    }

    private static List<SimpleGrantedAuthority> getSimpleGrantedAuthorities(UserEntity userEntity) {
        return userEntity.getRoleEntitySet().stream()
                .flatMap(role -> Stream.concat(
                        Stream.of(new SimpleGrantedAuthority("ROLE_" + role.getRoleEnum().name())),
                        role.getPermissionEntities().stream().map(permission -> new SimpleGrantedAuthority(permission.getName()))
                ))
                .collect(Collectors.toList());
    }

    public AuthResponse loginUser(AuthLoginRequest authLoginRequest){
        String username = authLoginRequest.username();
        String password = authLoginRequest.password();

        Authentication authentication = this.authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtil.createToken(authentication);
        AuthResponse authResponse = new AuthResponse(username, "User logged successfully", accessToken, true);
        return authResponse;
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

    public AuthResponse createUser(AuthCreateUserRequest userRequest) {
        String username = userRequest.username();
        String password = userRequest.password();
        List<String> rolList = userRequest.roleRequest().roleListName();
        Set<RoleEntity> rolesDb = (Set<RoleEntity>) roleRepository.findRoleEntitiesByRoleEnumIn(rolList);
        if(rolesDb.isEmpty()){
            throw new IllegalArgumentException("The roles specified do not exist");
        }

        UserEntity userEntity = UserEntity.builder()
                .username(username)
                .password(password)
                .roleEntitySet(rolesDb)
                .isEnabled(true)
                .accountNoExpired(true)
                .accountNoLocked(true)
                .credentialNoExpired(true)
                .build();

        UserEntity userCreated = userRepository.save(userEntity);
        List<SimpleGrantedAuthority> authorities = getSimpleGrantedAuthorities(userCreated);
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(username, password, authorities);
        String accessToken = jwtUtil.createToken(authenticationToken);
        return new AuthResponse(userCreated.getUsername(),"User created successfully", accessToken,true);
    }
}