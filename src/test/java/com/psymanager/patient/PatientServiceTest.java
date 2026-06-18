package com.psymanager.patient;

import com.psymanager.common.exception.ConflictException;
import com.psymanager.common.exception.ResourceNotFoundException;
import com.psymanager.patient.dto.PatientRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository repository;

    @InjectMocks
    private PatientService service;

    /** CPF de teste válido (dígitos verificadores corretos). */
    private static final String VALID_CPF_MASKED = "529.982.247-25";
    private static final String VALID_CPF_DIGITS = "52998224725";

    private PatientRequest request(String cpf) {
        return new PatientRequest("Maria Silva", cpf, LocalDate.of(1990, 1, 1),
                "11999999999", "maria@example.com", PatientStatus.ACTIVE, null, null, null);
    }

    @Test
    void createWithDuplicateCpfThrowsConflict() {
        when(repository.existsByCpf(VALID_CPF_DIGITS)).thenReturn(true);

        assertThatThrownBy(() -> service.create(request(VALID_CPF_MASKED)))
                .isInstanceOf(ConflictException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void createWithNullCpfSkipsUniquenessCheckAndSaves() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(request(null));

        verify(repository, never()).existsByCpf(any());
        verify(repository).save(any());
    }

    @Test
    void createNormalizesCpfBeforeSaving() {
        when(repository.existsByCpf(VALID_CPF_DIGITS)).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);

        service.create(request(VALID_CPF_MASKED));

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCpf()).isEqualTo(VALID_CPF_DIGITS);
    }

    @Test
    void updateMissingPatientThrowsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request(null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deactivateSetsStatusInactive() {
        Patient patient = new Patient();
        patient.setStatus(PatientStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(patient));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.deactivate(1L);

        assertThat(patient.getStatus()).isEqualTo(PatientStatus.INACTIVE);
    }
}
