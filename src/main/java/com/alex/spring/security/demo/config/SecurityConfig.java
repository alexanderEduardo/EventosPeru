package com.alex.spring.security.demo.config;

import com.alex.spring.security.demo.auth.LoginSuccessHandler;
import com.alex.spring.security.demo.exception.CustomAuthenticationEntryPoint;
import com.alex.spring.security.demo.filters.JwtTokenValidator;
import com.alex.spring.security.demo.interceptors.JwtCookieInterceptor;
import com.alex.spring.security.demo.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    //@Autowired
    //AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    private LoginSuccessHandler successHandler;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        /*
        * CSRF: se recomienda configurarlo cuando se use spring mvc con formularios
        * la session debe ser stateless cuando es un app rest ya que no usa la session del servidor sino jwt por ejemplo, si fuera una mvc deberia existir la session
        * */

        return httpSecurity
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/auth/**", "/api/**")// Ignorar CSRF para estas rutas
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**","/login", "/public/**","/test/**","/auth/**","/register").permitAll()
                        .requestMatchers("/home").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated()  // Cualquier otra solicitud debe estar autenticada
                )
                .exceptionHandling(exception->exception.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                .httpBasic( basic ->
                        basic.disable())
                .formLogin(
                        form -> form.disable()
                        /*.loginPage("/login")
                        .successHandler(successHandler)
                        .permitAll()*/
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                /*.logout( logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())*/
                .addFilterBefore(new JwtTokenValidator(jwtUtil), BasicAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService detailsService, PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(detailsService);
        //provider.setForcePrincipalAsString(true);
        return provider;
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoderByCrypt(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new JwtCookieInterceptor(httpServletRequest));
        return restTemplate;
    }

}
