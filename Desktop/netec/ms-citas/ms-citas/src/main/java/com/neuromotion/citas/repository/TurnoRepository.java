package com.neuromotion.citas.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.neuromotion.citas.model.Turno;

public interface TurnoRepository extends MongoRepository<Turno, String> {
    List<Turno> findByDoctorIdAndSedeId(String doctorId, String sedeId);
    List<Turno> findByDoctorId(String doctorId);
  // Nueva consulta más específica
    List<Turno> findByDoctorIdAndEspecialidadIdAndSedeId(String doctorId, String especialidadId, String sedeId);
        
    @Query("{ 'doctorId': ?0, 'especialidadId': ?1, 'sedeId': ?2, 'diasDisponibles.fecha': ?3 }")
    List<Turno> findByDoctorIdAndEspecialidadIdAndSedeIdAndFecha(
        String doctorId, 
        String especialidadId, 
        String sedeId, 
        LocalDate fecha
    );
    @Query("{ 'diasDisponibles.fecha': ?0 }")
    List<Turno> findByFecha(LocalDate fecha);

    @Query("{ 'doctorId': ?0, 'diasDisponibles.fecha': ?1 }")
    List<Turno> findByDoctorIdAndFecha(String doctorId, LocalDate fecha);

}