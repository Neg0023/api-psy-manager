package com.psymanager.appointment.dto;

import com.psymanager.appointment.AppointmentStatus;

import java.time.OffsetDateTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        String patientName,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        AppointmentStatus status,
        String notes) {
}
