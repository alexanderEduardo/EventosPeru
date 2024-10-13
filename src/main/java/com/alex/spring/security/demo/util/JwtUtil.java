package com.alex.spring.security.demo.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${security.jwt.key.private}")
    private String privateKey;

    @Value("${security.jwt.user.generator}")
    private String userGenerator;

    private Algorithm algorithm;

    @PostConstruct
    public void init() {
        this.algorithm = Algorithm.HMAC256(privateKey);
    }

    public String createToken(Authentication authentication){
        try {
            String username = authentication.getPrincipal().toString();

            String authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            String jwt = JWT.create()
                    .withIssuer(userGenerator)
                    .withSubject(username)
                    .withClaim("authorities",authorities)
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + 1800000))
                    .withJWTId(UUID.randomUUID().toString())
                    .withNotBefore(new Date())
                    .sign(algorithm);
            return jwt;
        }catch (JWTCreationException e){
            throw new JWTCreationException("An error has occurred in createToken, more details: ".concat(e.getMessage()),e.getCause());
        }

    }

    public DecodedJWT validateToken(String token){
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(userGenerator)
                    .build();
            return verifier.verify(token);
        }catch (JWTVerificationException e){
            throw new JWTVerificationException("An error has occurred in validateToken method, more details: ".concat(e.getMessage()));
        }
    }

    public String extractUsername(DecodedJWT decodedJWT){
        return decodedJWT.getSubject();
    }

    public Claim getSpecificClaim(DecodedJWT decodedJWT, String claimName){
        return decodedJWT.getClaim(claimName);
    }

    public Map<String, Claim> getAllClaims(DecodedJWT decodedJWT){
        return decodedJWT.getClaims();
    }

}
