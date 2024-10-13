package com.alex.spring.security.demo.config;

import com.alex.spring.security.demo.filters.JwtTokenValidator;
import com.alex.spring.security.demo.util.JwtUtil;
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

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    //@Autowired
    //AuthenticationConfiguration authenticationConfiguration;

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        /*
        * CSRF: se recomienda configurarlo cuando se use spring mvc con formularios
        * la session debe ser stateless cuando es un app rest ya que no usa la session del servidor sino jwt por ejemplo, si fuera una mvc deberia existir la session
        * */

        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(http->{
                    http.requestMatchers(HttpMethod.POST, "/auth/**").permitAll();
                    http.requestMatchers(HttpMethod.GET, "/method/hello").permitAll();
                    http.requestMatchers(HttpMethod.GET, "/method/hello-secured").hasAuthority("READ");
                    http.requestMatchers(HttpMethod.POST, "/method/post").hasAnyRole("ADMIN");
                    http.requestMatchers(HttpMethod.PUT, "/method/put").hasAnyAuthority("UPDATE");
                    http.requestMatchers(HttpMethod.DELETE, "/method/delete").hasAnyRole("INVITED");
                   //http.requestMatchers(HttpMethod.DELETE, "/auth/delete").hasAnyAuthority("DELETE");
                    http.anyRequest().denyAll();
                    //http.anyRequest().authenticated();
                })
                //.addFilterBefore(new JwtTokenValidator(jwtUtil), BasicAuthenticationFilter.class)
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

    @Bean(name = "userDetailsServiceInMemoryBean")
    public UserDetailsService userDetailsService(){
        List<UserDetails> userDetails = new ArrayList<>();
        userDetails.add(User.withUsername("admin")
                .password("1234")
                .roles("ADMIN")
                .authorities("READ","CREATE").build());
        userDetails.add(User.withUsername("alex")
                .password("1234")
                .roles("USER")
                .authorities("READ").build());
        return new InMemoryUserDetailsManager(userDetails);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoderByCrypt(){
        return new BCryptPasswordEncoder();
    }

}
