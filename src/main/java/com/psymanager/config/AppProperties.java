package com.psymanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurações específicas da aplicação (prefixo {@code app} no application.yml).
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(String adminEmail, String encryptionKey, Cors cors, Google google) {

    /** Origem do frontend liberada no CORS. */
    public record Cors(String allowedOrigin) {
    }

    /** Dados do OAuth do Google (login + Forms API). */
    public record Google(String clientId, String clientSecret, String redirectUri) {
    }
}
