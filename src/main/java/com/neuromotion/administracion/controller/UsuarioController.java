package com.neuromotion.administracion.controller;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.neuromotion.administracion.dto.PacienteResponse;
import com.neuromotion.administracion.dto.RegistroDoctorRequest;
import com.neuromotion.administracion.dto.RegistroRequest;
import com.neuromotion.administracion.dto.UsuarioPasswordChangeRequest;
import com.neuromotion.administracion.dto.UsuarioUpdateRequest;
import com.neuromotion.administracion.enums.Rol;
import com.neuromotion.administracion.model.Usuario;
import com.neuromotion.administracion.repository.UsuarioRepository;
import com.neuromotion.administracion.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @GetMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PACIENTE')")
    public ResponseEntity<?> obtenerUsuario(@PathVariable String id, Authentication authentication) {
       return usuarioService.obtenerUsuario(id, authentication);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PACIENTE')")
    public ResponseEntity<?> actualizarUsuario(@PathVariable String id,
                                               @Valid @RequestBody UsuarioUpdateRequest updateRequest,
                                               Authentication authentication) {
      return usuarioService.actualizarUsuario(id, updateRequest, authentication);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String id) {
       return usuarioService.eliminarUsuario(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody RegistroDoctorRequest usuarioDto, Authentication authentication) {
       return usuarioService.crearUsuario(usuarioDto, authentication);
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PACIENTE')")
    public ResponseEntity<?> cambiarPassword(@PathVariable String id,
                                            @Valid @RequestBody UsuarioPasswordChangeRequest request,
                                         Authentication authentication) {
     return usuarioService.cambiarPassword(id, request, authentication);
}

    @GetMapping("/pacientes")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PacienteResponse> listarPacientes() {
        return usuarioRepository.findByRolesContaining(Rol.PACIENTE);
    }
}