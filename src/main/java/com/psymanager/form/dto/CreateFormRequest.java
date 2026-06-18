package com.psymanager.form.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateFormRequest(
        @NotBlank(message = "O título do formulário é obrigatório")
        String title,

        Long patientId,

        @NotEmpty(message = "Inclua ao menos uma pergunta")
        @Valid
        List<FormQuestionDto> questions) {
}
