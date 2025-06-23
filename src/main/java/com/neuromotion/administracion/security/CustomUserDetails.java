package com.neuromotion.administracion.security;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.neuromotion.administracion.enums.Rol;
import com.neuromotion.administracion.model.Usuario;

public class CustomUserDetails implements UserDetails {
    private String username; // tipoDocumento-numeroDocumento
    private String password;
    private String usuarioId;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombre;
    private String apellidos;
    private String direccion;
    private String celular;
    private String correo;

    private Set<Rol> roles;
    
    public CustomUserDetails(Usuario usuario) {
        this.username = usuario.getUsername(); // tipoDocumento-numeroDocumento
        this.password = usuario.getPassword();
        this.usuarioId = usuario.getId();
        this.tipoDocumento = usuario.getTipoDocumento();
        this.numeroDocumento = usuario.getNumeroDocumento();
        this.nombre = usuario.getNombres();
        this.roles = usuario.getRoles();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());
    }
    
    public String getUsuarioId() {
        return usuarioId;
    }
    
    public String getTipoDocumento() {
        return tipoDocumento;
    }
    
    public String getNumeroDocumento() {
        return numeroDocumento;
    }
    
    public String getNombre() {
        return nombre;
    }

    public Set<Rol> getRoles() {
        return roles;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}