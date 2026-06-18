package com.psymanager.form;

import com.psymanager.form.dto.AnamnesisFormResponse;
import com.psymanager.form.dto.CreateFormRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
public class FormController {

    private final GoogleOAuthService googleOAuthService;
    private final AnamnesisFormService formService;

    public FormController(GoogleOAuthService googleOAuthService, AnamnesisFormService formService) {
        this.googleOAuthService = googleOAuthService;
        this.formService = formService;
    }

    /** Retorna a URL de consentimento do Google (chamada pelo frontend autenticado). */
    @GetMapping("/api/integrations/google/consent")
    public Map<String, String> consentUrl() {
        return Map.of("url", googleOAuthService.buildConsentUrl());
    }

    /** Callback do Google (navegação do navegador, sem Bearer — liberado no Security). */
    @GetMapping("/api/integrations/google/callback")
    public ResponseEntity<String> callback(@RequestParam(required = false) String code,
                                           @RequestParam(required = false) String error) {
        if (error != null) {
            return ResponseEntity.badRequest().body("Consentimento negado: " + error);
        }
        googleOAuthService.handleCallback(code);
        return ResponseEntity.ok("Conta Google conectada com sucesso. Você já pode fechar esta aba.");
    }

    @PostMapping("/api/forms")
    public ResponseEntity<AnamnesisFormResponse> create(@Valid @RequestBody CreateFormRequest request) {
        AnamnesisFormResponse created = formService.createForm(request);
        return ResponseEntity.created(URI.create("/api/forms/" + created.id())).body(created);
    }

    @GetMapping("/api/forms")
    public List<AnamnesisFormResponse> list(@RequestParam(required = false) Long patientId) {
        return formService.list(patientId);
    }
}
