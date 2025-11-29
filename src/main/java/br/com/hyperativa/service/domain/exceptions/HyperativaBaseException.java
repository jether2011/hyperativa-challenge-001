package br.com.hyperativa.service.domain.exceptions;

import java.io.Serial;

public class HyperativaBaseException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public HyperativaBaseException(String message) {
        super(message);
    }

    public HyperativaBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public HyperativaBaseException(Throwable cause) {
        super(cause);
    }
}
