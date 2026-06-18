package com.psymanager.patient.dto;

import com.psymanager.patient.PatientStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

/**
 * Dados de entrada para criação/atualização de paciente.
 *
 * <p>O CPF é <b>opcional</b>: quando informado, é validado pelo {@link CPF}
 * (formato + dígitos verificadores); quando nulo/vazio, é aceito.
 */
public record PatientRequest(
        @NotBlank(message = "Nome completo é obrigatório")
        String fullName,

        @CPF(message = "CPF inválido")
        String cpf,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve ser no passado")
        LocalDate birthDate,

        @NotBlank(message = "Telefone é obrigatório")
        String phone,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotNull(message = "Status é obrigatório")
        PatientStatus status,

        String profession,

        String address,

        String notes) {
}
