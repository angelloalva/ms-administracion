package com.neuromotion.citas.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.neuromotion.citas.components.AdministrationClient;
import com.neuromotion.citas.components.AuthenticationClient;
import com.neuromotion.citas.dto.CitaResponse;
import com.neuromotion.citas.dto.DoctorResponse;
import com.neuromotion.citas.dto.UsuarioResponse;
import com.neuromotion.citas.exceptions.HorarioNoDisponibleException;
import com.neuromotion.citas.model.Cita;
import com.neuromotion.citas.model.Doctor;
import com.neuromotion.citas.model.Especialidad;
import com.neuromotion.citas.model.Sede;
import com.neuromotion.citas.model.Turno;

import com.neuromotion.citas.repository.CitaRepository;
import com.neuromotion.citas.repository.TurnoRepository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CitaService {

    private final TurnoRepository turnoRepository;
    private final CitaRepository citaRepository;
       // Clientes para comunicación con microservicios
    private final AuthenticationClient authenticationClient;
    private final AdministrationClient  administrationClient;
    public Cita crearCita(Cita cita) {
        if (!estaDisponible(cita.getDoctorId(), cita.getSedeId(), cita.getFechaHora())) {
            List<LocalDateTime> horarios = obtenerHorariosDisponibles(
                    cita.getDoctorId(),
                    cita.getSedeId(),
                    cita.getFechaHora().toLocalDate());

            throw new HorarioNoDisponibleException("El doctor no tiene disponibilidad en esa fecha, hora o sede.",
                    horarios);
        }
        // Buscar el turno y slot correspondiente y marcarlo como ocupado
        /*
         * List<Turno> turnos =
         * turnoRepository.findByDoctorIdAndSedeId(cita.getDoctorId(),
         * cita.getSedeId());
         * for (Turno turno : turnos) {
         * for (Turno.DiaTurno dia : turno.getDiasDisponibles()) {
         * if (dia.getFecha().equals(cita.getFechaHora().toLocalDate())) {
         * for (Turno.SlotTurno slot : dia.getSlots()) {
         * if (slot.getFechaHoraCompleta().equals(cita.getFechaHora())) {
         * slot.setOcupado(true);
         * turnoRepository.save(turno); // Guarda el turno actualizado
         * break;
         * }
         * }
         * }
         * }
         * }
         */
        // Buscar el turno y slot correspondiente y marcarlo como ocupado (optimizado)
        List<Turno> turnos = turnoRepository.findByDoctorIdAndSedeId(cita.getDoctorId(), cita.getSedeId());
        turnos.stream()
                .flatMap(turno -> turno.getDiasDisponibles().stream()
                        .filter(dia -> dia.getFecha().equals(cita.getFechaHora().toLocalDate()))
                        .flatMap(dia -> dia.getSlots().stream()
                                .filter(slot -> slot.getFechaHoraCompleta().equals(cita.getFechaHora()))
                                .peek(slot -> {
                                    slot.setOcupado(true);
                                    turnoRepository.save(turno); // Guarda el turno actualizado
                                })))
                .findFirst();
        return citaRepository.save(cita);
    }

    public boolean estaDisponible(String doctorId, String sedeId, LocalDateTime fechaHora) {
        // 1. Obtener los turnos del doctor para la sede
        List<Turno> turnos = turnoRepository.findByDoctorIdAndSedeId(doctorId, sedeId);

        // 2. Verificar si la fecha y hora están dentro de algún turno
        boolean dentroDeTurno = turnos.stream()
                .anyMatch(turno -> turno.getDiasDisponibles().stream()
                        .anyMatch(dia -> dia.getFecha().equals(fechaHora.toLocalDate()) &&
                                !fechaHora.toLocalTime().isBefore(dia.getHoraInicio()) &&
                                !fechaHora.toLocalTime().isAfter(dia.getHoraFin())));

        if (!dentroDeTurno) {
            return false;
        }

        // 3. Verificar si ya existe cita en esa fecha y hora para el doctor
        return !citaRepository.existsByDoctorIdAndFechaHora(doctorId, fechaHora);
    }

    public List<LocalDateTime> obtenerHorariosDisponibles(String doctorId, String sedeId, LocalDate fecha) {
        List<Turno> turnos = turnoRepository.findByDoctorIdAndSedeId(doctorId, sedeId);

        return turnos.stream()
                .flatMap(turno -> turno.getDiasDisponibles().stream())
                .filter(dia -> dia.getFecha().equals(fecha))
                .flatMap(dia -> dia.getSlots().stream())
                .filter(slot -> !slot.isOcupado())
                .map(Turno.SlotTurno::getFechaHoraCompleta)
                .toList();
    }

    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> buscarPorId(String id) {
        return citaRepository.findById(id);
    }

    public List<CitaResponse> obtenerCitasPorPaciente(String pacienteId,String authorizationHeader) {
        List<Cita> citas = citaRepository.findByPacienteId(pacienteId);
        log.info("Citas encontradas para pacienteId {}: {}", pacienteId, citas.size());
  // Cargar doctores para el especialidadId
        Map<String, DoctorResponse> doctorsMap = authenticationClient.getAllDoctors(authorizationHeader).stream()
                .collect(Collectors.toMap(DoctorResponse::getUsuarioId, doctor -> doctor)); // Cambiado a getUsuarioId
        log.info("Doctores cargados: {}", doctorsMap.size());
        // Cargar usuarios para nombres y apellidos
        Map<String, UsuarioResponse> usersMap = authenticationClient.getAllUsers(authorizationHeader).stream()
                .collect(Collectors.toMap(UsuarioResponse::getId, user -> user));
        log.info("Usuarios cargados: {}", usersMap.size());

        // Cargar sedes para el nombre de la sede
        Map<String, Sede> sedesMap = administrationClient.getAllSedes(authorizationHeader).stream()
                .collect(Collectors.toMap(Sede::getId, sede -> sede));
        log.info("Sedes cargadas: {}", sedesMap.size());

      

        // Cargar especialidades para el nombre de la especialidad
        Map<String, Especialidad> especialidadesMap = administrationClient.getAllEspecialidades(authorizationHeader).stream()
                .collect(Collectors.toMap(Especialidad::getId, especialidad -> especialidad));
        log.info("Especialidades cargadas: {}", especialidadesMap.size());

        return citas.stream()
                .map(cita -> {
                    DoctorResponse doctor = doctorsMap.get(cita.getDoctorId());
                    String especialidadId = doctor != null ? doctor.getEspecialidadId() : null;
                    Especialidad especialidad = especialidadId != null ? especialidadesMap.get(especialidadId) : null;
                    log.info("Cita ID: {}, Doctor ID: {}, Especialidad ID: {}, Especialidad Nombre: {}",
                            cita.getId(), cita.getDoctorId(), especialidadId,
                            especialidad != null ? especialidad.getNombre() : "No encontrada");
                    return CitaResponse.fromCita(
                            cita,
                            usersMap.get(cita.getDoctorId()),
                            especialidad,
                            sedesMap.get(cita.getSedeId()));
                })
                .collect(Collectors.toList());
    }

    public List<Cita> listarPorDoctor(String doctorId) {
        return citaRepository.findByDoctorId(doctorId);
    }

    public void eliminarCita(String id) {
        citaRepository.deleteById(id);
    }

}