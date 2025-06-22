package com.neuromotion.administracion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.neuromotion.administracion.model.Doctor;

public interface DoctorRepository  extends MongoRepository<Doctor, String> {
    // Búsquedas básicas
    Optional<Doctor> findByCmp(String cmp);
    List<Doctor> findBySedeIdsContaining(String sedeId);
    List<Doctor> findByEspecialidadId(String especialidadId);
    List<Doctor> findByUsuarioIdIn(List<String> usuarioIds);
    List<Doctor> findByEspecialidadIdAndSedeIdsContaining(String especialidadId, String sedeId);
    List<Doctor> findByUsuarioId(String usuarioId);
    Optional<Doctor> findById(String id);

    // Verificar existencia
    boolean existsByCmp(String cmp);
    
    // Consultas personalizadas
    @Query("{ 'especialidad': ?0, 'turnos': { $exists: true, $not: { $size: 0 } } }")
    List<Doctor> findByEspecialidadWithTurnos(String especialidad);
    boolean existsByUsuarioId(String usuarioId);
}