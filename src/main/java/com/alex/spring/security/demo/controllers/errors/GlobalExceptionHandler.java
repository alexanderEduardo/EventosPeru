package com.alex.spring.security.demo.controllers.errors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    // Captura los errores de validación cuando un campo está vacío o es incorrecto
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        // Recorre cada error de campo y lo agrega al mapa de errores
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("errorType", "VALIDATION_ERROR");
        errorDetails.put("message", "Error de validación: algunos campos contienen errores");
        errorDetails.put("errors", errors);
        errorDetails.put("path", request.getRequestURI());

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        Map<String, String> errorDetails = new HashMap<>();
        if (ex.getMessage().contains("Duplicate entry")) {
            errorDetails.put("errorType", "EMAIL_DUPLICATE");
            errorDetails.put("status", String.valueOf(HttpStatus.CONFLICT.value()));
            errorDetails.put("error", "Duplicate entry");
            errorDetails.put("message", "El email ya está registrado. Por favor, use otro.");
            return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
        }
        errorDetails.put("errorType", "DATA_INTEGRITY_EXCEPTION");
        errorDetails.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
        errorDetails.put("error", "Data integrity violation");
        errorDetails.put("message", "Error de integridad de datos");
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

}
