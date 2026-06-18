package com.psymanager.appointment;

import com.psymanager.appointment.dto.AppointmentRequest;
import com.psymanager.appointment.dto.AppointmentResponse;
import com.psymanager.common.exception.BadRequestException;
import com.psymanager.common.exception.ConflictException;
import com.psymanager.common.exception.ResourceNotFoundException;
import com.psymanager.patient.Patient;
import com.psymanager.patient.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class AppointmentService {

    /** Status que NÃO bloqueiam o horário (sessões canceladas liberam o slot). */
    private static final Set<AppointmentStatus> CANCELLED = EnumSet.of(
            AppointmentStatus.CANCELED_BY_PATIENT, AppointmentStatus.CANCELED_BY_PROFESSIONAL);

    private final AppointmentRepository repository;
    private final PatientRepository patientRepository;

    public AppointmentService(AppointmentRepository repository, PatientRepository patientRepository) {
        this.repository = repository;
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listInRange(OffsetDateTime start, OffsetDateTime end) {
        return repository
                .findByStartTimeLessThanAndEndTimeGreaterThanOrderByStartTime(end, start)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        AppointmentStatus status = request.status() != null ? request.status() : AppointmentStatus.SCHEDULED;
        validateTimes(request.startTime(), request.endTime());
        ensureNoOverlap(request.startTime(), request.endTime(), null, status);

        Appointment appointment = new Appointment();
        appointment.setPatient(loadPatient(request.patientId()));
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());
        appointment.setStatus(status);
        appointment.setNotes(request.notes());
        return AppointmentMapper.toResponse(repository.save(appointment));
    }

    @Transactional
    public AppointmentResponse update(Long id, AppointmentRequest request) {
        Appointment appointment = findOrThrow(id);
        AppointmentStatus status = request.status() != null ? request.status() : appointment.getStatus();
        validateTimes(request.startTime(), request.endTime());
        ensureNoOverlap(request.startTime(), request.endTime(), id, status);

        appointment.setPatient(loadPatient(request.patientId()));
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());
        appointment.setStatus(status);
        appointment.setNotes(request.notes());
        return AppointmentMapper.toResponse(repository.save(appointment));
    }

    @Transactional
    public AppointmentResponse updateStatus(Long id, AppointmentStatus status) {
        Appointment appointment = findOrThrow(id);
        // Ao reativar uma sessão (sair de cancelada), revalida o choque de horário.
        if (!CANCELLED.contains(status)) {
            ensureNoOverlap(appointment.getStartTime(), appointment.getEndTime(), id, status);
        }
        appointment.setStatus(status);
        return AppointmentMapper.toResponse(repository.save(appointment));
    }

    @Transactional
    public void delete(Long id) {
        Appointment appointment = findOrThrow(id);
        repository.delete(appointment);
    }

    private void validateTimes(OffsetDateTime start, OffsetDateTime end) {
        if (!end.isAfter(start)) {
            throw new BadRequestException("O horário de término deve ser posterior ao de início");
        }
    }

    private void ensureNoOverlap(OffsetDateTime start, OffsetDateTime end, Long excludeId, AppointmentStatus status) {
        if (CANCELLED.contains(status)) {
            return; // sessões canceladas não ocupam horário
        }
        if (repository.existsOverlapping(start, end, excludeId, CANCELLED)) {
            throw new ConflictException("Já existe uma sessão nesse horário (choque de agenda)");
        }
    }

    private Patient loadPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado: " + patientId));
    }

    private Appointment findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada: " + id));
    }
}
