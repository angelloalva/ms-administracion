package com.neuromotion.citas.repository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.neuromotion.citas.model.Cita;

public interface CitaRepository extends MongoRepository<Cita, String> {
    boolean existsByDoctorIdAndFechaHora(String doctorId, LocalDateTime fechaHora);
    List<Cita> findByPacienteId(String pacienteId);
    List<Cita> findByDoctorId(String doctorId);
    List<Cita> findBySedeId(String sedeId);
}