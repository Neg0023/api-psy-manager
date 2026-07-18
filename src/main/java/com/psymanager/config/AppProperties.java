package com.psymanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configurações específicas da aplicação (prefixo {@code app} no application.yml).
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(String adminEmail, String encryptionKey, Cors cors, Google google) {

    /** Origens do frontend liberadas no CORS (lista, sem curinga por causa das credenciais). */
    public record Cors(List<String> allowedOrigins) {
    }

    /** Dados do OAuth do Google (login + Forms API). */
    public record Google(String clientId, String clientSecret, String redirectUri) {
    }
}
