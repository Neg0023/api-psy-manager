package com.psymanager.finance.dto;

import com.psymanager.finance.EntryStatus;
import com.psymanager.finance.EntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialEntryRequest(
        @NotBlank(message = "Descrição é obrigatória")
        String description,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal amount,

        @NotNull(message = "Data de competência é obrigatória")
        LocalDate competenceDate,

        @NotNull(message = "Tipo é obrigatório")
        EntryType type,

        @NotNull(message = "Status é obrigatório")
        EntryStatus status,

        Long patientId) {
}
