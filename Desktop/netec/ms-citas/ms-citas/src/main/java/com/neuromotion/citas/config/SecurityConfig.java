package com.neuromotion.citas.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.neuromotion.citas.security.JwtRequestFilter;

@EnableMethodSecurity(prePostEnabled = true)
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtRequestFilter jwtRequestFilter; // Tu filtro adaptado

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/health", "/actuator/**").permitAll() // Health checks
                .requestMatchers("/health", "/health/**").permitAll() // Health checks
                .requestMatchers("/actuator/health", "/actuator/info").permitAll() // Actuator básico
                .requestMatchers("/actuator/**").hasRole("ADMIN") // Otros actuator solo admin
                // Todos los endpoints de citas requieren autenticación
                .requestMatchers(HttpMethod.GET, "/citas/**").hasAnyRole("ADMIN", "DOCTOR", "PACIENTE")
                .requestMatchers(HttpMethod.POST, "/citas/**").hasAnyRole("ADMIN", "PACIENTE")
                .requestMatchers(HttpMethod.PUT, "/citas/**").hasAnyRole("ADMIN", "PACIENTE")
                .requestMatchers(HttpMethod.DELETE, "/citas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/turnos/**").hasAnyRole("ADMIN", "DOCTOR", "PACIENTE")
                .requestMatchers(HttpMethod.POST, "/turnos/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers(HttpMethod.PUT, "/turnos/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers(HttpMethod.DELETE, "/turnos/**").hasAnyRole("ADMIN", "DOCTOR")
                
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Solo agregar el filtro JWT
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}