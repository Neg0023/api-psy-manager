package com.psymanager.common.exception;

/** Lançada em violações de integridade de negócio (ex.: CPF duplicado). Mapeada para HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
