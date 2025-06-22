package com.neuromotion.administracion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.neuromotion.administracion.components.CitasClient;
import com.neuromotion.administracion.dto.CitaResponse;
import com.neuromotion.administracion.dto.DoctorCreateRequest;
import com.neuromotion.administracion.dto.DoctorResponse;
import com.neuromotion.administracion.dto.DoctorUpdateRequest;
import com.neuromotion.administracion.dto.PacienteResponse;
import com.neuromotion.administracion.enums.Rol;
import com.neuromotion.administracion.model.Cita;
import com.neuromotion.administracion.model.Doctor;
import com.neuromotion.administracion.model.Especialidad;
import com.neuromotion.administracion.model.Usuario;

import com.neuromotion.administracion.repository.DoctorRepository;
import com.neuromotion.administracion.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {
    
  
    private final DoctorRepository doctorRepository;

    private final UsuarioRepository usuarioRepository;
    
    private final EspecialidadService especialidadService;
 private final CitasClient citasClient;
    
    // Validar especialidad antes de crear doctor
   /*public EspecialidadValidationResponse validarEspecialidad(String especialidadNombre) {
        if (especialidadNombre == null || especialidadNombre.trim().isEmpty()) {
            return new EspecialidadValidationResponse(false, "La especialidad no puede estar vacía", null, null);
        }
        
        String nombreLimpio = especialidadNombre.trim();
        List<Especialidad> todasEspecialidades = especialidadService.listar();
        
        // Buscar coincidencia exacta (ignorando mayúsculas)
        Optional<Especialidad> coincidenciaExacta = todasEspecialidades.stream()
                .filter(esp -> esp.getNombre().equalsIgnoreCase(nombreLimpio))
                .findFirst();
        
        if (coincidenciaExacta.isPresent()) {
            return new EspecialidadValidationResponse(
                true, 
                "Especialidad encontrada", 
                coincidenciaExacta.get(), 
                null
            );
        }
        
        // Buscar especialidades similares (para sugerir)
        List<Especialidad> similares = todasEspecialidades.stream()
                .filter(esp -> sonSimilares(esp.getNombre(), nombreLimpio))
                .limit(3)
                .collect(Collectors.toList());
        
        return new EspecialidadValidationResponse(
            false, 
            similares.isEmpty() ? "Especialidad no encontrada" : "Especialidad no encontrada, pero hay similares", 
            null, 
            similares
        );
    }
    */
    // Método para determinar si dos nombres son similares
    private boolean sonSimilares(String nombre1, String nombre2) {
        String n1 = nombre1.toLowerCase().replaceAll("[\\s-_]", "");
        String n2 = nombre2.toLowerCase().replaceAll("[\\s-_]", "");
        
        // Verificar si uno contiene al otro o viceversa
        return n1.contains(n2) || n2.contains(n1) || 
               calcularSimilitudLevenshtein(n1, n2) > 0.7;
    }
    
    // Algoritmo básico de similitud (puedes usar librerías como Apache Commons Text)
    private double calcularSimilitudLevenshtein(String s1, String s2) {
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLen;
    }
    
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i-1) == s2.charAt(j-1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i-1][j] + 1, dp[i][j-1] + 1), dp[i-1][j-1] + cost);
            }
        }
        return dp[s1.length()][s2.length()];
    }
    // Crear doctor
    public DoctorResponse crearDoctor(DoctorCreateRequest request) {
        // Validar que el usuario exista
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(request.getUsuarioId());
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("No existe un usuario con el ID: " + request.getUsuarioId());
        }

        // Validar que no exista un doctor con el mismo CMP
        if (doctorRepository.existsByCmp(request.getCmp())) {
            throw new IllegalArgumentException("Ya existe un doctor con el CMP: " + request.getCmp());
        }

        // Validar que no exista ya un doctor vinculado a ese usuario
        if (doctorRepository.existsByUsuarioId(request.getUsuarioId())) {
            throw new IllegalArgumentException("Ya existe un doctor vinculado a este usuario.");
        }

        // Validar especialidad
        if (request.getEspecialidadId() == null || request.getEspecialidadId().trim().isEmpty()) {
            throw new IllegalArgumentException("La especialidad es obligatoria.");
        }
        Optional<Especialidad> especialidadOpt = especialidadService.buscarPorId(request.getEspecialidadId());
        if (especialidadOpt.isEmpty()) {
            throw new IllegalArgumentException("La especialidad indicada no existe.");
        }

        // Crear el doctor solo con los campos obligatorios
        Doctor doctor = new Doctor();
        doctor.setUsuarioId(request.getUsuarioId());
        doctor.setCmp(request.getCmp());
        doctor.setEspecialidadId(request.getEspecialidadId());
        doctor.setSedeIds(request.getSedeIds() != null ? request.getSedeIds() : new ArrayList<>());
        doctor.setFotoUrl(request.getFotoUrl()); // Puede ser null

        Doctor doctorGuardado = doctorRepository.save(doctor);
        return DoctorResponse.fromDoctor(doctorGuardado);
    }
    
    // Obtener todos los doctores
    public List<DoctorResponse> obtenerTodosLosDoctores() {
        return doctorRepository.findAll().stream()
                .map(DoctorResponse::fromDoctor)
                .collect(Collectors.toList());
    }
    
    // Obtener doctor por ID
    public Optional<DoctorResponse> obtenerDoctorPorId(String id) {
        return doctorRepository.findById(id)
                .map(DoctorResponse::fromDoctor);
    }
    
    // Obtener doctor por CMP
    public Optional<DoctorResponse> obtenerDoctorPorCmp(String cmp) {
        return doctorRepository.findByCmp(cmp)
                .map(DoctorResponse::fromDoctor);
    }
    
    // Obtener doctores por especialidad
    public List<DoctorResponse> obtenerDoctoresPorEspecialidad(String especialidadId) {
        return doctorRepository.findByEspecialidadId(especialidadId).stream()
                .map(DoctorResponse::fromDoctor)
                .collect(Collectors.toList());
    }
    
    // Obtener doctores por sede
    public List<DoctorResponse> obtenerDoctoresPorSede(String sedeId) {
        return doctorRepository.findBySedeIdsContaining(sedeId).stream()
                .map(DoctorResponse::fromDoctor)
                .collect(Collectors.toList());
    }
    
    // Buscar doctores por nombre
    public List<DoctorResponse> buscarDoctoresPorNombre(String nombre) {
        // 1. Buscar usuarios con rol DOCTOR y nombre que coincida (ignorar mayúsculas)
        List<Usuario> usuarios = usuarioRepository.findByNombresContainingIgnoreCaseAndRolesContaining(nombre, Rol.DOCTOR);

        // 2. Obtener los IDs de usuario
        List<String> usuarioIds = usuarios.stream()
                .map(Usuario::getId)
                .collect(Collectors.toList());

        if (usuarioIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. Buscar doctores cuyo usuarioId esté en la lista
        List<Doctor> doctores = doctorRepository.findByUsuarioIdIn(usuarioIds);

        // 4. Mapear a DoctorResponse
        return doctores.stream()
                .map(DoctorResponse::fromDoctor)
                .collect(Collectors.toList());
    }
    
    // Buscar doctores por nombre y rol
    public List<DoctorResponse> buscarDoctoresPorNombreYRol(String nombre, Rol rol) {
        // Buscar usuarios con el nombre y rol especificados
        List<Usuario> usuarios = usuarioRepository.findByNombresContainingIgnoreCaseAndRolesContaining(nombre, rol);

        // Obtener los IDs de usuario
        List<String> usuarioIds = usuarios.stream()
                .map(Usuario::getId)
                .collect(Collectors.toList());

        if (usuarioIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Buscar doctores cuyo usuarioId esté en la lista
        List<Doctor> doctores = doctorRepository.findByUsuarioIdIn(usuarioIds);

        // Mapear a DoctorResponse
        return doctores.stream()
                .map(DoctorResponse::fromDoctor)
                .collect(Collectors.toList());
    }
    
    // Actualizar doctor
   /* public Optional<DoctorResponse> actualizarDoctor(String id, DoctorUpdateRequest request) {
        return doctorRepository.findById(id).map(doctor -> {
            // Actualizar CMP si se envía y es diferente
            if (request.getCmp() != null && !request.getCmp().equals(doctor.getCmp())) {
                // Validar que no exista otro doctor con el mismo CMP
                if (doctorRepository.existsByCmp(request.getCmp())) {
                    throw new IllegalArgumentException("Ya existe un doctor con el CMP: " + request.getCmp());
                }
                doctor.setCmp(request.getCmp());
            }
            // Actualizar especialidadId si se envía
            if (request.getEspecialidadId() != null) {
                Optional<Especialidad> especialidadOpt = especialidadService.buscarPorId(request.getEspecialidadId());
                if (especialidadOpt.isEmpty()) {
                    throw new IllegalArgumentException("La especialidad indicada no existe.");
                }
                doctor.setEspecialidadId(request.getEspecialidadId());
            }
            // Actualizar sedes si se envía
            if (request.getSedeIds() != null) {
                doctor.setSedeIds(request.getSedeIds());
            }
            // Actualizar foto si se envía
            if (request.getFotoUrl() != null) {
                doctor.setFotoUrl(request.getFotoUrl());
            }

            Doctor doctorActualizado = doctorRepository.save(doctor);
            return DoctorResponse.fromDoctor(doctorActualizado);
        });
    }
*/
    // Eliminar doctor
    public boolean eliminarDoctor(String id) {
        if (doctorRepository.existsById(id)) {
            doctorRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    

    public List<Usuario> obtenerPacientesDeDoctor(String usuarioId,String authorizationHeader) {
        // Buscar todas las citas donde doctorId == usuarioId
        List<CitaResponse> citas = citasClient.getCitas(usuarioId,authorizationHeader);
       if (citas.isEmpty()) {
        return new ArrayList<>();
    }

    Set<String> pacienteIds = citas.stream()
        .map(CitaResponse::getPacienteId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

        return usuarioRepository.findAllById(pacienteIds).stream()
            .filter(u -> u.getRoles().contains(Rol.PACIENTE))
            .collect(Collectors.toList());
    }
}