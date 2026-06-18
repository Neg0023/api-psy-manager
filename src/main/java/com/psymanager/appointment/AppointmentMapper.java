package com.psymanager.appointment;

import com.psymanager.appointment.dto.AppointmentResponse;

public final class AppointmentMapper {

    private AppointmentMapper() {
    }

    public static AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getPatient().getId(),
                a.getPatient().getFullName(),
                a.getStartTime(),
                a.getEndTime(),
                a.getStatus(),
                a.getNotes());
    }
}
