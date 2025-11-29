package br.com.hyperativa.service.application.web.errors;

import br.com.hyperativa.service.domain.exceptions.*;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RestExceptionHandler.class);
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";

    @ExceptionHandler({
            HyperativaBaseException.class,
            UserCreateException.class,
            CardCreateException.class,
            UserAlreadyExistsException.class
    })
    public ResponseEntity<Object> handleHyperativaBaseException(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put(MESSAGE, ex.getMessage());

        switch (ex) {
            case HyperativaBaseException hyperativaBaseException -> body.put(ERROR, "Hyperativa Base Exception");
            case UserCreateException userCreateException -> body.put(ERROR, "User Create Exception");
            case CardCreateException cardCreateException -> body.put(ERROR, "Card Create Exception");
            case UserAlreadyExistsException userAlreadyExistsException ->
                    body.put(ERROR, "User Already Exists Exception");
            default -> body.put(ERROR, "Unknown Exception");
        }

        LOG.error("Exception: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put(ERROR, "Resource Not Found");
        body.put(MESSAGE, ex.getMessage());

        LOG.error("Exception: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({Exception.class, FileUploadException.class})
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put(ERROR, "Internal Server Error");
        body.put(MESSAGE, ex.getMessage());

        LOG.error("Exception: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
