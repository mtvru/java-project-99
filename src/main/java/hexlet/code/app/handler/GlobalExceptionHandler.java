package hexlet.code.app.handler;

import hexlet.code.app.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@ControllerAdvice
@Slf4j
public final class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String INTEGRITY_VIOLATION_MSG =
        "Data integrity violation. Check if this record already exists.";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return this.buildErrorResponse(ex, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex) {
        return this.buildErrorResponse(ex, HttpStatus.CONFLICT, INTEGRITY_VIOLATION_MSG);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        return this.buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        return this.buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String shortFieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
            validationErrors.put(shortFieldName, violation.getMessage());
        });
        return this.buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Validation failed", validationErrors);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request
    ) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            validationErrors.put(error.getField(), error.getDefaultMessage())
        );
        return this.buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Validation failed", validationErrors);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request
    ) {
        return this.buildErrorResponse(ex, statusCode, "An unexpected error occurred.");
    }

    private ResponseEntity<Object> buildErrorResponse(Exception ex, HttpStatusCode status, String message) {
        Map<String, String> errors = new HashMap<>();
        return this.buildErrorResponse(ex, status, message, errors);
    }

    private ResponseEntity<Object> buildErrorResponse(
        Exception ex, HttpStatusCode status, String message, Map<String, String> errors
    ) {
        String traceId = UUID.randomUUID().toString();
        if (!status.equals(HttpStatus.NOT_FOUND)) {
            log.error("[traceId: {}] {}: ", traceId, message, ex);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("traceId", traceId);
        body.put("status", status.value());
        body.put("message", message);
        body.put("errors", errors);
        return new ResponseEntity<>(body, status);
    }
}
