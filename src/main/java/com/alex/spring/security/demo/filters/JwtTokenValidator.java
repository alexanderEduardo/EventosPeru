package com.alex.spring.security.demo.filters;

import com.alex.spring.security.demo.persistence.entity.utils.UserCustomDetails;
import com.alex.spring.security.demo.services.IUserService;
import com.alex.spring.security.demo.util.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

public class JwtTokenValidator extends OncePerRequestFilter {

    private JwtUtil jwtUtil;

    public JwtTokenValidator(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain filterChain) throws ServletException, IOException {
        String jwtToken = null;

        // Verificar si hay un Bearer Token en el header Authorization
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            jwtToken = bearerToken.substring(7);
        }

        if (jwtToken != null) {
            try {
                DecodedJWT decodedJWT = jwtUtil.validateToken(jwtToken);

                String username = jwtUtil.extractUsername(decodedJWT);
                String stringAuthorities = jwtUtil.getSpecificClaim(decodedJWT, "authorities").asString();
                String email = jwtUtil.getSpecificClaim(decodedJWT,"email").asString();

                Collection<? extends GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(stringAuthorities);
                // Cargar el usuario desde el servicio de usuario

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                Authentication authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                ((UsernamePasswordAuthenticationToken) authenticationToken).setDetails(new UserCustomDetails(email));
                context.setAuthentication(authenticationToken);
                SecurityContextHolder.setContext(context);
            } catch (Exception e) {
                // Manejar el error de validación del token si es necesario
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return; // Salir si el token no es válido
            }
        }

        filterChain.doFilter(request,response);
    }
}
