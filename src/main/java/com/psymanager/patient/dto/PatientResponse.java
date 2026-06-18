package com.psymanager.patient.dto;

import com.psymanager.patient.PatientStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Dados de saída de um paciente. */
public record PatientResponse(
        Long id,
        String fullName,
        String cpf,
        LocalDate birthDate,
        String phone,
        String email,
        PatientStatus status,
        String profession,
        String address,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
