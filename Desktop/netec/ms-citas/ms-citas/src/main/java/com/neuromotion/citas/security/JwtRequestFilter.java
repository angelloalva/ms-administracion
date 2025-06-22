package com.neuromotion.citas.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import com.neuromotion.citas.util.JwtUtil;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response, 
                                  FilterChain filterChain)
                                  throws ServletException, IOException {
        
             final String authorizationHeader = request.getHeader("Authorization");

        String jwt = null;
        boolean isValidToken = false;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                // Solo validar que el token sea válido (firma correcta y no expirado)
                isValidToken = jwtUtil.validateToken(jwt);
            } catch (JwtException e) {
                // Token inválido - continuar sin autenticación
                logger.warn("Token JWT inválido: " + e.getMessage());
            }
        }

        // Si el token es válido, establecer una autenticación básica
        if (isValidToken && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Crear una autenticación simple que indica que el token es válido
            // Extraer roles del token válido
            List<String> roles = jwtUtil.extractRoles(jwt);
            List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
            
            PreAuthenticatedAuthenticationToken authToken =
                new PreAuthenticatedAuthenticationToken("authenticated_user", jwt, authorities);

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
