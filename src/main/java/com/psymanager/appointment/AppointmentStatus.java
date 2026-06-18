package com.psymanager.appointment;

/** Estados possíveis de uma sessão. */
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    DONE,
    CANCELED_BY_PATIENT,
    CANCELED_BY_PROFESSIONAL
}
