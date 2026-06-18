package com.psymanager.appointment.dto;

import com.psymanager.appointment.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record AppointmentStatusUpdateRequest(
        @NotNull(message = "Status é obrigatório") AppointmentStatus status) {
}
