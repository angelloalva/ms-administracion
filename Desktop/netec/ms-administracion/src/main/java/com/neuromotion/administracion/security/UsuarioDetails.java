package com.neuromotion.administracion.security;

import com.neuromotion.administracion.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class UsuarioDetails implements UserDetails {

    private final Usuario usuario;

    public UsuarioDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
            .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.name()))
            .collect(Collectors.toSet());
        return authorities;
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        // Concatenamos tipo y número como lo usas en login
        return usuario.getTipoDocumento() + "|" + usuario.getNumeroDocumento();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // o agregar lógica si manejas expiración
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // o agregar lógica si manejas bloqueo
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // o agregar lógica si manejas expiración de credenciales
    }

    @Override
    public boolean isEnabled() {
        return true; // o agregar campo activo en Usuario
    }

    public Usuario getUsuario() {
        return usuario;
    }
}

