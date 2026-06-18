package com.psymanager.form.dto;

import java.time.OffsetDateTime;

public record AnamnesisFormResponse(
        Long id,
        Long patientId,
        String patientName,
        String googleFormId,
        String shareUrl,
        String title,
        OffsetDateTime createdAt) {
}
