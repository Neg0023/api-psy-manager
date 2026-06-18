package com.psymanager.appointment.dto;

import com.psymanager.appointment.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

/** Dados de criação/atualização de sessão. {@code status} é opcional (default SCHEDULED). */
public record AppointmentRequest(
        @NotNull(message = "Paciente é obrigatório")
        Long patientId,

        @NotNull(message = "Horário de início é obrigatório")
        OffsetDateTime startTime,

        @NotNull(message = "Horário de término é obrigatório")
        OffsetDateTime endTime,

        AppointmentStatus status,

        String notes) {
}
