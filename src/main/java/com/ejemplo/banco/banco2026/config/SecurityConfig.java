package com.ejemplo.banco.banco2026.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String API_PATTERN = "/api/**";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                // CSRF deshabilitado de forma consciente: la API es REST, sin estado (STATELESS) y se autentica
                // con HTTP Basic en cada petición; no usa cookies de sesión, que es el vector que explota CSRF.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Reglas de Autorización
                .authorizeHttpRequests(auth -> auth
                        // 1. Swagger y documentación son públicos
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 2. Operaciones de escritura (POST, PUT, DELETE) solo para ADMIN
                        .requestMatchers(HttpMethod.POST, API_PATTERN).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT, API_PATTERN).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, API_PATTERN).hasRole(ROLE_ADMIN)

                        // 3. Operaciones de lectura (GET) para USER y ADMIN
                        .requestMatchers(HttpMethod.GET, API_PATTERN).hasAnyRole(ROLE_USER, ROLE_ADMIN)

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @org.springframework.beans.factory.annotation.Value("${USER_PASSWORD:root}")
    private String userPassword;

    @org.springframework.beans.factory.annotation.Value("${ADMIN_PASSWORD:root}")
    private String adminPassword;

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder.encode(userPassword))
                .roles(ROLE_USER)
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode(adminPassword))
                .roles(ROLE_ADMIN)
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}