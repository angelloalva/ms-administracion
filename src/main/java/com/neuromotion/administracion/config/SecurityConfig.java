package com.neuromotion.administracion.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.neuromotion.administracion.security.JwtRequestFilter;

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
                .requestMatchers(HttpMethod.GET, "/especialidades/**").hasAnyRole("ADMIN", "DOCTOR", "PACIENTE")
                .requestMatchers(HttpMethod.POST, "/especialidades/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/especialidades/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/especialidades/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/sedes/**").hasAnyRole("ADMIN", "DOCTOR", "PACIENTE")
                .requestMatchers(HttpMethod.POST, "/sedes/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/sedes/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/sedes/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/usuarios/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/usuarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/usuarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/doctores/**").hasAnyRole("ADMIN", "DOCTOR", "PACIENTE")
                .requestMatchers(HttpMethod.POST, "/doctores/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/doctores/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Solo agregar el filtro JWT
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}