package com.psymanager.form.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Uma pergunta do formulário. {@code options} só se aplica a MULTIPLE_CHOICE. */
public record FormQuestionDto(
        @NotBlank(message = "O texto da pergunta é obrigatório")
        String text,

        @NotNull(message = "O tipo da pergunta é obrigatório")
        FormQuestionType type,

        List<String> options) {
}
