package com.psymanager.patient;

import com.psymanager.common.exception.ConflictException;
import com.psymanager.config.SecurityConfig;
import com.psymanager.patient.dto.PatientResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test de segurança + validação do endpoint de Pacientes.
 *
 * <p>O {@link JwtDecoder} é mockado para evitar a criação do decoder real (que
 * faria chamada de rede ao Google na inicialização).
 */
@WebMvcTest(PatientController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.admin-email=admin@test.com",
        "app.google.client-id=client-123",
        "app.cors.allowed-origin=http://localhost:5173",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://accounts.google.com"
})
class PatientControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final String VALID_BODY = """
            {
              "fullName": "Maria Silva",
              "cpf": "529.982.247-25",
              "birthDate": "1990-01-01",
              "phone": "11999999999",
              "email": "maria@example.com",
              "status": "ACTIVE"
            }
            """;

    private static SimpleGrantedAuthority adminRole() {
        return new SimpleGrantedAuthority("ROLE_ADMIN");
    }

    private static PatientResponse sampleResponse() {
        return new PatientResponse(1L, "Maria Silva", "52998224725", LocalDate.of(1990, 1, 1),
                "11999999999", "maria@example.com", PatientStatus.ACTIVE, null, null, null,
                OffsetDateTime.now(), OffsetDateTime.now());
    }

    @Test
    void unauthenticatedRequestIsRejectedWith401() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedButNonAdminIsRejectedWith403() throws Exception {
        mockMvc.perform(get("/api/patients").with(jwt())) // token sem ROLE_ADMIN
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreatePatient() throws Exception {
        when(patientService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/patients")
                        .with(jwt().authorities(adminRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void invalidCpfReturns400() throws Exception {
        String invalidCpfBody = VALID_BODY.replace("529.982.247-25", "111.111.111-11");

        mockMvc.perform(post("/api/patients")
                        .with(jwt().authorities(adminRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidCpfBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateCpfReturns409() throws Exception {
        when(patientService.create(any())).thenThrow(new ConflictException("CPF duplicado"));

        mockMvc.perform(post("/api/patients")
                        .with(jwt().authorities(adminRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict());
    }
}
