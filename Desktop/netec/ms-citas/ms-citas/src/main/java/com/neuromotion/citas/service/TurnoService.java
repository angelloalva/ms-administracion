package com.neuromotion.citas.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.neuromotion.citas.components.AdministrationClient;
import com.neuromotion.citas.components.AuthenticationClient;
import com.neuromotion.citas.dto.DoctorResponse;
import com.neuromotion.citas.dto.TurnoResponse;
import com.neuromotion.citas.dto.UsuarioResponse;
import com.neuromotion.citas.enums.DiaSemana;
import com.neuromotion.citas.exceptions.DocumentoNoEncontradoException;
import com.neuromotion.citas.model.Especialidad;
import com.neuromotion.citas.model.Sede;
import com.neuromotion.citas.model.Turno;
import com.neuromotion.citas.repository.TurnoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
public class TurnoService {

   private final TurnoRepository turnoRepository;
       // Clientes para comunicación con microservicios
    private final AuthenticationClient authenticationClient;
    private final AdministrationClient  administrationClient;
 // ================= CREAR TURNO =================
    public Turno crearTurno(Turno nuevoTurno) {
        if (nuevoTurno.getDoctorId() == null || nuevoTurno.getEspecialidadId() == null ||
            nuevoTurno.getSedeId() == null || nuevoTurno.getDiasDisponibles() == null ||
            nuevoTurno.getDiasDisponibles().isEmpty()) {
            throw new IllegalArgumentException("Faltan datos obligatorios para crear el turno");
        }
        log.info("Creando turno para doctor {} en sede {} y especialidad {}", 
                nuevoTurno.getDoctorId(), nuevoTurno.getSedeId(), nuevoTurno.getEspecialidadId());
        // Validar que las fechas correspondan con los días
        validarConsistenciaFechasDias(nuevoTurno);
        
        // Validar conflictos antes de guardar
        validarConflictosHorarios(nuevoTurno);
        
        // Generar slots de 15 minutos para cada día
        nuevoTurno.getDiasDisponibles().forEach(this::generarSlots);
        
        return turnoRepository.save(nuevoTurno);
    }
    
    // ================= OBTENER TURNOS =================
    public List<TurnoResponse> obtenerTodosLosTurnos(String authorizationHeader) {
       // Obtener todos los turnos
        List<Turno> turnos = turnoRepository.findAll();

        // Crear mapas para lookups eficientes
        Map<String, UsuarioResponse> usuariosMap = authenticationClient.getAllUsers(authorizationHeader).stream()
                .collect(Collectors.toMap(UsuarioResponse::getId, usuario -> usuario));
        Map<String, Sede> sedesMap =administrationClient.getAllSedes(authorizationHeader).stream()
                .collect(Collectors.toMap(Sede::getId, sede -> sede));
        Map<String, Especialidad> especialidadesMap = administrationClient.getAllEspecialidades(authorizationHeader).stream()
                .collect(Collectors.toMap(Especialidad::getId, especialidad -> especialidad));
        Map<String, DoctorResponse> doctorMap = authenticationClient.getAllDoctors(authorizationHeader).stream()
                .collect(Collectors.toMap(DoctorResponse::getUsuarioId, doctor -> doctor));
        // Cruzar los datos
        return turnos.stream()
                .map(turno -> {
                    UsuarioResponse doctor = usuariosMap.getOrDefault(turno.getDoctorId(), new UsuarioResponse());
                    Sede sede = sedesMap.getOrDefault(turno.getSedeId(), new Sede());
                    Especialidad especialidad = especialidadesMap.getOrDefault(turno.getEspecialidadId(), new Especialidad());
                    DoctorResponse doctorInfo = doctorMap.get(turno.getDoctorId());
                    return TurnoResponse.fromTurno(turno, doctor, sede, especialidad,doctorInfo);
                })
                .collect(Collectors.toList());
    }

    public List<Turno> obtenerTurnosPorDoctor(String doctorId) {
        return turnoRepository.findByDoctorId(doctorId);
    }
    
    public List<Turno> obtenerTurnosPorFecha(LocalDate fecha) {
        return turnoRepository.findByFecha(fecha);
    }
    
    public List<Turno> obtenerTurnosPorDoctorYFecha(String doctorId, LocalDate fecha) {
        return turnoRepository.findByDoctorIdAndFecha(doctorId, fecha);
    }
    
    public Turno obtenerTurnoPorId(String turnoId) {
        return turnoRepository.findById(turnoId)
            .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado: " + turnoId));
    }
    
    // ================= MODIFICAR TURNOS =================
    public Turno modificarHorarioTurno(String turnoId, LocalDate fecha, 
                                      LocalTime nuevaHoraInicio, LocalTime nuevaHoraFin) {
        Turno turno = obtenerTurnoPorId(turnoId);
        
        Turno.DiaTurno diaAModificar = turno.getDiasDisponibles().stream()
            .filter(dia -> dia.getFecha().equals(fecha))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Fecha no encontrada en el turno: " + fecha));
        
        // Verificar que no hay citas ocupadas fuera del nuevo rango
        verificarCitasEnNuevoRango(diaAModificar, nuevaHoraInicio, nuevaHoraFin);
        
        diaAModificar.setHoraInicio(nuevaHoraInicio);
        diaAModificar.setHoraFin(nuevaHoraFin);
        
        // Regenerar slots
        generarSlots(diaAModificar);
        
        return turnoRepository.save(turno);
    }
    
    public Turno cambiarFechaTurno(String turnoId, LocalDate fechaAntigua, LocalDate fechaNueva) {
        Turno turno = obtenerTurnoPorId(turnoId);
        
        Turno.DiaTurno diaACambiar = turno.getDiasDisponibles().stream()
            .filter(dia -> dia.getFecha().equals(fechaAntigua))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Fecha no encontrada: " + fechaAntigua));
        
        // Verificar que no hay citas ocupadas
        if (diaACambiar.getSlots().stream().anyMatch(Turno.SlotTurno::isOcupado)) {
            throw new IllegalArgumentException("No se puede cambiar la fecha porque hay citas programadas");
        }
        
        // Validar que la nueva fecha no tenga conflictos
        validarFechaSinConflictos(turno.getDoctorId(), turno.getEspecialidadId(), 
                                 turno.getSedeId(), fechaNueva, diaACambiar);
        
        diaACambiar.setFecha(fechaNueva);
        diaACambiar.setDia(DiaSemana.valueOf(fechaNueva.getDayOfWeek().name()));
        
        // Regenerar slots con la nueva fecha
        generarSlots(diaACambiar);
        
        return turnoRepository.save(turno);
    }
    
    public Turno agregarDiaATurno(String turnoId, Turno.DiaTurno nuevoDia) {
        Turno turno = obtenerTurnoPorId(turnoId);
        
        // Validar consistencia fecha-día
        validarConsistenciaFechaDia(nuevoDia);
        
        // Validar que no hay conflictos
        validarFechaSinConflictos(turno.getDoctorId(), turno.getEspecialidadId(), 
                                 turno.getSedeId(), nuevoDia.getFecha(), nuevoDia);
        
        // Generar slots
        generarSlots(nuevoDia);
        
        turno.getDiasDisponibles().add(nuevoDia);
        
        return turnoRepository.save(turno);
    }
    
    // ================= ELIMINAR TURNOS =================
    public void eliminarTurno(String turnoId) {
        Turno turno = obtenerTurnoPorId(turnoId);
        
        // Verificar que no hay citas ocupadas
        boolean hayCitasOcupadas = turno.getDiasDisponibles().stream()
            .flatMap(dia -> dia.getSlots().stream())
            .anyMatch(Turno.SlotTurno::isOcupado);
            
        if (hayCitasOcupadas) {
            throw new IllegalArgumentException("No se puede eliminar el turno porque hay citas programadas");
        }
        
        turnoRepository.deleteById(turnoId);
    }
    
    public Turno eliminarDiaTurno(String turnoId, LocalDate fecha) {
        Turno turno = obtenerTurnoPorId(turnoId);
        
        Turno.DiaTurno diaAEliminar = turno.getDiasDisponibles().stream()
            .filter(dia -> dia.getFecha().equals(fecha))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Fecha no encontrada: " + fecha));
        
        // Verificar que no hay citas ocupadas en ese día
        if (diaAEliminar.getSlots().stream().anyMatch(Turno.SlotTurno::isOcupado)) {
            throw new IllegalArgumentException("No se puede eliminar el día porque hay citas programadas");
        }
        
        turno.getDiasDisponibles().removeIf(dia -> dia.getFecha().equals(fecha));
        
        // Si no quedan días, eliminar todo el turno
        if (turno.getDiasDisponibles().isEmpty()) {
            turnoRepository.deleteById(turnoId);
            return null;
        }
        
        return turnoRepository.save(turno);
    }
    
    // ================= GESTIÓN DE CITAS =================
    public Turno ocuparSlot(String turnoId, LocalDate fecha, LocalTime horaInicio, 
                           String pacienteId, String citaId) {
        Turno turno = obtenerTurnoPorId(turnoId);
        
        Turno.DiaTurno dia = turno.getDiasDisponibles().stream()
            .filter(d -> d.getFecha().equals(fecha))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Fecha no encontrada: " + fecha));
        
        Turno.SlotTurno slot = dia.getSlots().stream()
            .filter(s -> s.getHoraInicio().equals(horaInicio))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Slot no encontrado: " + horaInicio));
        
        if (slot.isOcupado()) {
            throw new IllegalArgumentException("El slot ya está ocupado");
        }
        
        slot.setOcupado(true);
        slot.setPacienteId(pacienteId);
        slot.setCitaId(citaId);
        
        return turnoRepository.save(turno);
    }
    
    public Turno liberarSlot(String turnoId, LocalDate fecha, LocalTime horaInicio) {
        Turno turno = obtenerTurnoPorId(turnoId);
        
        Turno.DiaTurno dia = turno.getDiasDisponibles().stream()
            .filter(d -> d.getFecha().equals(fecha))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Fecha no encontrada: " + fecha));
        
        Turno.SlotTurno slot = dia.getSlots().stream()
            .filter(s -> s.getHoraInicio().equals(horaInicio))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Slot no encontrado: " + horaInicio));
        
        slot.setOcupado(false);
        slot.setPacienteId(null);
        slot.setCitaId(null);
        
        return turnoRepository.save(turno);
    }
    
    // ================= CONSULTAS DE DISPONIBILIDAD =================
    public List<Turno.SlotTurno> obtenerSlotsDisponibles(String doctorId, String especialidadId, 
                                                         String sedeId, LocalDate fecha) {
        List<Turno> turnos = turnoRepository.findByDoctorIdAndEspecialidadIdAndSedeIdAndFecha(
            doctorId, especialidadId, sedeId, fecha);
        
        return turnos.stream()
            .flatMap(turno -> turno.getDiasDisponibles().stream())
            .filter(dia -> dia.getFecha().equals(fecha))
            .flatMap(dia -> dia.getSlots().stream())
            .filter(slot -> !slot.isOcupado())
            .collect(Collectors.toList());
    }
    
    public Turno duplicarTurnoAFechas(String turnoId, List<LocalDate> nuevasFechas) {
        Turno turnoOriginal = obtenerTurnoPorId(turnoId);
        
        if (turnoOriginal.getDiasDisponibles().isEmpty()) {
            throw new IllegalArgumentException("El turno original no tiene días disponibles");
        }
        
        Turno nuevoTurno = new Turno();
        nuevoTurno.setEspecialidadId(turnoOriginal.getEspecialidadId());
        nuevoTurno.setSedeId(turnoOriginal.getSedeId());
        nuevoTurno.setDoctorId(turnoOriginal.getDoctorId());
        
        // Usar el primer día como template
        Turno.DiaTurno template = turnoOriginal.getDiasDisponibles().get(0);
        
        List<Turno.DiaTurno> nuevosDias = nuevasFechas.stream()
            .map(fecha -> crearDiaTurnoDesdeTemplate(template, fecha))
            .collect(Collectors.toList());
        
        nuevoTurno.setDiasDisponibles(nuevosDias);
        
        // Validar conflictos
        validarConflictosHorarios(nuevoTurno);
        
        // Generar slots para cada día
        nuevoTurno.getDiasDisponibles().forEach(this::generarSlots);
        
        return turnoRepository.save(nuevoTurno);
    }
    
    // ================= MÉTODOS PRIVADOS =================
    private void generarSlots(Turno.DiaTurno diaTurno) {
        List<Turno.SlotTurno> slots = new ArrayList<>();
        LocalTime horaActual = diaTurno.getHoraInicio();
        
        while (horaActual.isBefore(diaTurno.getHoraFin())) {
            LocalTime horaFin = horaActual.plusMinutes(15);
            
            // No crear slot si se pasa del horario límite
            if (horaFin.isAfter(diaTurno.getHoraFin())) {
                break;
            }
            
            Turno.SlotTurno slot = new Turno.SlotTurno();
            slot.setHoraInicio(horaActual);
            slot.setHoraFin(horaFin);
            slot.setOcupado(false);
            slot.setFechaHoraCompleta(LocalDateTime.of(diaTurno.getFecha(), horaActual));
            
            slots.add(slot);
            horaActual = horaFin;
        }
        
        diaTurno.setSlots(slots);
    }
    
    private void validarConflictosHorarios(Turno nuevoTurno) {
        for (Turno.DiaTurno diaNuevo : nuevoTurno.getDiasDisponibles()) {
            validarFechaSinConflictos(nuevoTurno.getDoctorId(), nuevoTurno.getEspecialidadId(),
                                     nuevoTurno.getSedeId(), diaNuevo.getFecha(), diaNuevo);
        }
    }
    
    private void validarFechaSinConflictos(String doctorId, String especialidadId, String sedeId,
                                          LocalDate fecha, Turno.DiaTurno diaNuevo) {
        List<Turno> turnosExistentes = turnoRepository
            .findByDoctorIdAndEspecialidadIdAndSedeIdAndFecha(doctorId, especialidadId, sedeId, fecha);
        
        boolean hayConflicto = turnosExistentes.stream()
            .flatMap(turno -> turno.getDiasDisponibles().stream())
            .filter(diaExistente -> diaExistente.getFecha().equals(fecha))
            .anyMatch(diaExistente -> hayConflictoHorario(diaNuevo, diaExistente));
        
        if (hayConflicto) {
            throw new IllegalArgumentException(
                String.format("Ya existe un turno para el doctor %s el %s %s en el horario especificado", 
                    doctorId, diaNuevo.getDia(), fecha)
            );
        }
    }
    
    private boolean hayConflictoHorario(Turno.DiaTurno diaNuevo, Turno.DiaTurno diaExistente) {
        return diaNuevo.getHoraInicio().isBefore(diaExistente.getHoraFin())
            && diaNuevo.getHoraFin().isAfter(diaExistente.getHoraInicio());
    }
    
    private void verificarCitasEnNuevoRango(Turno.DiaTurno dia, LocalTime nuevaHoraInicio, 
                                           LocalTime nuevaHoraFin) {
        boolean hayCitasFueraDelRango = dia.getSlots().stream()
            .filter(Turno.SlotTurno::isOcupado)
            .anyMatch(slot -> slot.getHoraInicio().isBefore(nuevaHoraInicio) || 
                             slot.getHoraFin().isAfter(nuevaHoraFin));
        
        if (hayCitasFueraDelRango) {
            throw new IllegalArgumentException(
                "No se puede modificar el horario porque hay citas programadas fuera del nuevo rango"
            );
        }
    }
    
    private void validarConsistenciaFechasDias(Turno turno) {
        for (Turno.DiaTurno dia : turno.getDiasDisponibles()) {
            validarConsistenciaFechaDia(dia);
        }
    }
    
    private void validarConsistenciaFechaDia(Turno.DiaTurno dia) {
        DiaSemana diaCalculado = DiaSemana.fromJavaDayOfWeek(dia.getFecha().getDayOfWeek());
        if (!dia.getDia().equals(diaCalculado)) {
            throw new IllegalArgumentException(
                String.format("La fecha %s no corresponde al día %s", 
                    dia.getFecha(), dia.getDia())
            );
        }
    }

    
    private Turno.DiaTurno crearDiaTurnoDesdeTemplate(Turno.DiaTurno template, LocalDate nuevaFecha) {
        Turno.DiaTurno nuevoDia = new Turno.DiaTurno();
        nuevoDia.setFecha(nuevaFecha);
        nuevoDia.setDia(DiaSemana.valueOf(nuevaFecha.getDayOfWeek().name()));
        nuevoDia.setHoraInicio(template.getHoraInicio());
        nuevoDia.setHoraFin(template.getHoraFin());
        return nuevoDia;
    }

     public void countTurnos() {
        long count = turnoRepository.count();
        if (count == 0) {
            throw new DocumentoNoEncontradoException("No hay sedes registradas");
        }   
    }
}