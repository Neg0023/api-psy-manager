package com.psymanager.appointment;

import com.psymanager.appointment.dto.AppointmentRequest;
import com.psymanager.appointment.dto.AppointmentResponse;
import com.psymanager.common.exception.BadRequestException;
import com.psymanager.common.exception.ConflictException;
import com.psymanager.patient.Patient;
import com.psymanager.patient.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AppointmentService service;

    private final OffsetDateTime start = OffsetDateTime.parse("2026-06-15T10:00:00-03:00");
    private final OffsetDateTime end = OffsetDateTime.parse("2026-06-15T11:00:00-03:00");

    private AppointmentRequest request(OffsetDateTime s, OffsetDateTime e, AppointmentStatus status) {
        return new AppointmentRequest(1L, s, e, status, null);
    }

    private Patient patient() {
        Patient p = new Patient();
        p.setId(1L);
        p.setFullName("Maria Silva");
        return p;
    }

    @Test
    void createThrowsConflictOnOverlap() {
        when(repository.existsOverlapping(eq(start), eq(end), isNull(), anyCollection())).thenReturn(true);

        assertThatThrownBy(() -> service.create(request(start, end, null)))
                .isInstanceOf(ConflictException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void createSavesWhenNoOverlap() {
        when(repository.existsOverlapping(eq(start), eq(end), isNull(), anyCollection())).thenReturn(false);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient()));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = service.create(request(start, end, null));

        assertThat(response.status()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(response.patientName()).isEqualTo("Maria Silva");
        verify(repository).save(any());
    }

    @Test
    void createThrowsBadRequestWhenEndNotAfterStart() {
        assertThatThrownBy(() -> service.create(request(end, start, null)))
                .isInstanceOf(BadRequestException.class);

        verify(repository, never()).existsOverlapping(any(), any(), any(), anyCollection());
        verify(repository, never()).save(any());
    }

    @Test
    void createWithCancelledStatusSkipsOverlapCheck() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient()));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(request(start, end, AppointmentStatus.CANCELED_BY_PATIENT));

        verify(repository, never()).existsOverlapping(any(), any(), any(), anyCollection());
        verify(repository).save(any());
    }
}
