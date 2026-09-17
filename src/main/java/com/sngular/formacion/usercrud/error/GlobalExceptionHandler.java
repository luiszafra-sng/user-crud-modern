package com.sngular.formacion.usercrud.error;

import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String CODE_PROPERTY = "code";
    private static final String FIELD_ERRORS_PROPERTY = "fieldErrors";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición contiene errores de validación.");
        body.setTitle("Validation Error");
        body.setProperty(CODE_PROPERTY, "VALIDATION_ERROR");
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : ""))
                .toList();
        body.setProperty(FIELD_ERRORS_PROPERTY, fieldErrors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleNotFound(UserNotFoundException ex) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        body.setTitle("User Not Found");
        body.setProperty(CODE_PROPERTY, "USER_NOT_FOUND");
        return body;
    }

    // Supuesto: la única constraint UNIQUE del modelo es users.email; toda
    // DataIntegrityViolationException aquí se atribuye a colisión de email.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleConflict(DataIntegrityViolationException ex) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "El email indicado ya está registrado.");
        body.setTitle("Email Already Exists");
        body.setProperty(CODE_PROPERTY, "EMAIL_ALREADY_EXISTS");
        return body;
    }

    // No se define handler genérico Exception: se deja que Spring emita 500 para
    // preservar el ejercicio de depuración del bug intencional (ver BUG.md).
}
