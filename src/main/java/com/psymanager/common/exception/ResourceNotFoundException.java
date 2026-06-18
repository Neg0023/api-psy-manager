package com.psymanager.common.exception;

/** Lançada quando um recurso solicitado não existe. Mapeada para HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
