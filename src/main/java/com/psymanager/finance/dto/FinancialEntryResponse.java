package com.psymanager.finance.dto;

import com.psymanager.finance.EntryStatus;
import com.psymanager.finance.EntryType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialEntryResponse(
        Long id,
        String description,
        BigDecimal amount,
        LocalDate competenceDate,
        EntryType type,
        EntryStatus status,
        Long patientId,
        String patientName) {
}
