package com.psymanager.finance;

import com.psymanager.finance.dto.FinancialEntryResponse;
import com.psymanager.patient.Patient;

public final class FinancialEntryMapper {

    private FinancialEntryMapper() {
    }

    public static FinancialEntryResponse toResponse(FinancialEntry e) {
        Patient patient = e.getPatient();
        return new FinancialEntryResponse(
                e.getId(),
                e.getDescription(),
                e.getAmount(),
                e.getCompetenceDate(),
                e.getType(),
                e.getStatus(),
                patient != null ? patient.getId() : null,
                patient != null ? patient.getFullName() : null);
    }
}
