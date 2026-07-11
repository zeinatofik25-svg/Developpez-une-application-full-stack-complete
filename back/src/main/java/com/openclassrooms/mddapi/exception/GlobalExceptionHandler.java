package com.openclassrooms.mddapi.exception;

import com.openclassrooms.mddapi.dto.error.ApiErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception, HttpServletRequest request) {
        LOGGER.warn("Erreur API contrôlée: status={}, path={}, message={}",
            exception.getStatus().value(), request.getRequestURI(), exception.getMessage());
        return buildResponse(exception.getStatus(), exception.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception,
                                                                      HttpServletRequest request) {
        Map<String, String> validationErrors = exception.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> fieldError.getDefaultMessage() == null ? "Valeur invalide" : fieldError.getDefaultMessage(),
                (first, second) -> first
            ));

        LOGGER.warn("Erreur de validation: path={}, errors={}", request.getRequestURI(), validationErrors);

        return buildResponse(HttpStatus.BAD_REQUEST, "Requete invalide", request, validationErrors);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiErrorResponse> handleJwtException(JwtException exception, HttpServletRequest request) {
        LOGGER.warn("Token JWT invalide: path={}, reason={}", request.getRequestURI(), exception.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Token JWT invalide", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        LOGGER.error("Erreur inattendue: path={}", request.getRequestURI(), exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur", request, null);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status,
                                                           String message,
                                                           HttpServletRequest request,
                                                           Map<String, String> validationErrors) {
        ApiErrorResponse body = new ApiErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI(),
            validationErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}