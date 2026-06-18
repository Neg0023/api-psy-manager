package com.psymanager.common.exception;

/** Lançada em requisições inválidas de regra de negócio. Mapeada para HTTP 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
