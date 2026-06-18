package com.psymanager.patient;

import com.psymanager.patient.dto.PatientRequest;
import com.psymanager.patient.dto.PatientResponse;

/** Conversões entre entidade e DTOs de paciente, além da normalização de CPF. */
public final class PatientMapper {

    private PatientMapper() {
    }

    /** Remove máscara do CPF; retorna {@code null} se vazio/nulo (CPF é opcional). */
    public static String normalizeCpf(String cpf) {
        if (cpf == null) {
            return null;
        }
        String digits = cpf.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }

    /** Aplica os dados do request na entidade, usando o CPF já normalizado. */
    public static void applyRequest(Patient patient, PatientRequest request, String normalizedCpf) {
        patient.setFullName(request.fullName());
        patient.setCpf(normalizedCpf);
        patient.setBirthDate(request.birthDate());
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setStatus(request.status());
        patient.setProfession(request.profession());
        patient.setAddress(request.address());
        patient.setNotes(request.notes());
    }

    public static PatientResponse toResponse(Patient p) {
        return new PatientResponse(
                p.getId(),
                p.getFullName(),
                p.getCpf(),
                p.getBirthDate(),
                p.getPhone(),
                p.getEmail(),
                p.getStatus(),
                p.getProfession(),
                p.getAddress(),
                p.getNotes(),
                p.getCreatedAt(),
                p.getUpdatedAt());
    }
}
