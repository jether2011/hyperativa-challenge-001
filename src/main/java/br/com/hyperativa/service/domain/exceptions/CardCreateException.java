package br.com.hyperativa.service.domain.exceptions;

public class CardCreateException extends RuntimeException {
    public CardCreateException(String message) {
        super(message);
    }

    public CardCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public CardCreateException(Throwable cause) {
        super(cause);
    }
}
