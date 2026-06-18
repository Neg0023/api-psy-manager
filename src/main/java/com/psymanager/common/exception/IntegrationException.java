package com.psymanager.common.exception;

/** Falha ao comunicar com um serviço externo (ex.: Google Forms API). Mapeada para HTTP 502. */
public class IntegrationException extends RuntimeException {

    public IntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
