package com.neuromotion.administracion.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.neuromotion.administracion.dto.DoctorCreateRequest;
import com.neuromotion.administracion.dto.MensajeResponse;
import com.neuromotion.administracion.dto.RegistroDoctorRequest;
import com.neuromotion.administracion.dto.RegistroRequest;
import com.neuromotion.administracion.dto.UsuarioPasswordChangeRequest;
import com.neuromotion.administracion.dto.UsuarioUpdateRequest;
import com.neuromotion.administracion.enums.Rol;
import com.neuromotion.administracion.exceptions.DocumentoNoEncontradoException;
import com.neuromotion.administracion.exceptions.DuplicateResourceException;
import com.neuromotion.administracion.model.Usuario;
import com.neuromotion.administracion.repository.UsuarioRepository;
import com.neuromotion.administracion.security.UsuarioDetails;
import com.neuromotion.administracion.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils authUtils;
    private final DoctorService doctorService;
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public ResponseEntity<?> obtenerUsuario(String id, Authentication authentication) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);
        if (optionalUsuario.isEmpty()) {
           throw new DocumentoNoEncontradoException("Usuario no encontrado");
        }

        Usuario actual = optionalUsuario.get();
        UsuarioDetails userDetails = (UsuarioDetails) authentication.getPrincipal();
        String usernameActual = userDetails.getUsername().split("\\|")[1];

        if (authUtils.esAdmin(authentication) || actual.getNumeroDocumento().equals(usernameActual)) {
            return ResponseEntity.ok(actual);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
    }

    public ResponseEntity<MensajeResponse> actualizarUsuario(String id, UsuarioUpdateRequest updateRequest, Authentication authentication) {
        return usuarioRepository.findById(id).map(usuario -> {
            String documentoAuth = authUtils.extraerDocumentoDeAuth(authentication);

            if (authUtils.esAdmin(authentication) || usuario.getNumeroDocumento().equals(documentoAuth)) {
               if (updateRequest.getNombres() != null) {
                    usuario.setNombres(updateRequest.getNombres());
                }
                if (updateRequest.getApellidos() != null) {
                    usuario.setApellidos(updateRequest.getApellidos());
                }
                if (updateRequest.getCelular() != null) {
                    usuario.setCelular(updateRequest.getCelular());
                }
                if (updateRequest.getCorreo() != null) {
                    usuario.setCorreo(updateRequest.getCorreo());
                }
                if (updateRequest.getDireccion() != null) {
                    usuario.setDireccion(updateRequest.getDireccion());
                }
                usuarioRepository.save(usuario);
                return ResponseEntity.ok(new MensajeResponse("Actualizado con éxito"));
            }

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MensajeResponse("Acceso denegado"));
        }).orElseThrow(() -> new DocumentoNoEncontradoException("Usuario no encontrado"));
    }

    public ResponseEntity<Void> eliminarUsuario(String id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<?> crearUsuario(RegistroDoctorRequest request, Authentication authentication) {
        try {
            logger.info("Request recibido para crear usuario: {}", request);

            logger.info("Intento de creación de usuario por: {}", authentication.getName());
            logger.info("Roles del creador: {}", authentication.getAuthorities());

            RegistroRequest usuarioDto = request.getUsuario();
            DoctorCreateRequest doctorDto = request.getDoctor();

            if (usuarioRepository.findByTipoDocumentoAndNumeroDocumento(usuarioDto.getDocumentoTipo(), usuarioDto.getDocumentoNumero()).isPresent()) {
                throw new DuplicateResourceException("Usuario ya existe");
            }

            Set<Rol> rolesDelCreador = SecurityUtils.getRolesFromAuthentication(authentication);
            Set<Rol> rolesPermitidos = new HashSet<>();
            if (rolesDelCreador.contains(Rol.ADMIN)) {
                rolesPermitidos.addAll(List.of(Rol.ADMIN, Rol.DOCTOR, Rol.PACIENTE));
            }
            /* else if (rolesDelCreador.contains(Rol.DOCTOR)) {
                rolesPermitidos.add(Rol.PACIENTE);
            } */
             

            if (usuarioDto.getRoles() == null || usuarioDto.getRoles().isEmpty()) {
                return ResponseEntity.badRequest().body(new MensajeResponse("Debe asignar al menos un rol"));
            }

            for (Rol rol : usuarioDto.getRoles()) {
                if (!rolesPermitidos.contains(rol)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para asignar el rol: " + rol);
                }
            }

            if (usuarioDto.getPassword() != null && !usuarioDto.getPassword().isBlank()) {
                logger.warn("Se ignoró la contraseña enviada por el solicitante. Se usará el número de documento como clave por defecto.");
            }

            Usuario nuevo = new Usuario();
            nuevo.setTipoDocumento(usuarioDto.getDocumentoTipo());
            nuevo.setNumeroDocumento(usuarioDto.getDocumentoNumero());
            nuevo.setNombres(usuarioDto.getNombres());
            nuevo.setApellidos(usuarioDto.getApellidos());
            nuevo.setCorreo(usuarioDto.getCorreo());
            nuevo.setCelular(usuarioDto.getCelular());
            nuevo.setDireccion(usuarioDto.getDireccion());
            nuevo.setPassword(passwordEncoder.encode(usuarioDto.getDocumentoNumero()));
            nuevo.setRoles(usuarioDto.getRoles());

            usuarioRepository.save(nuevo);

            // Si el usuario tiene el rol DOCTOR y se envió info de doctor, crear también el documento en la colección doctores
            if (usuarioDto.getRoles().contains(Rol.DOCTOR) && doctorDto != null) {
                DoctorCreateRequest doctorRequest = new DoctorCreateRequest(
                    nuevo.getId(),
                    doctorDto.getCmp(),
                    doctorDto.getEspecialidadId(),
                    doctorDto.getSedeIds(),
                    doctorDto.getFotoUrl()
                );
                doctorService.crearDoctor(doctorRequest);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(new MensajeResponse("Usuario creado"));
        } catch (Exception e) {
            logger.error("Error al crear usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MensajeResponse("Error al crear usuario"));
        }
    }

    public ResponseEntity<?> cambiarPassword(String id, UsuarioPasswordChangeRequest request, Authentication authentication) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findByNumeroDocumento(id);
        if (optionalUsuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario actual = optionalUsuario.get();
        String documentoAuth = authUtils.extraerDocumentoDeAuth(authentication);

        if (!authUtils.esAdmin(authentication) && !actual.getNumeroDocumento().equals(documentoAuth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MensajeResponse("Acceso denegado"));
        }

        if (!passwordEncoder.matches(request.getPasswordActual(), actual.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeResponse("La contraseña actual es incorrecta"));
        }

        if (!request.getNuevaPassword().equals(request.getConfirmarPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeResponse("La nueva contraseña no coincide con la confirmación"));
        }

        actual.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(actual);
        return ResponseEntity.ok(new MensajeResponse("Contraseña actualizada exitosamente"));
    }
}