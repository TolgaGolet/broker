package com.brokagefirm.broker.exception;

import com.brokagefirm.broker.exception.enums.GenericExceptionMessages;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class BrokerExceptionHandler {
    /*
     * Expected exceptions handler
     */
    @ExceptionHandler(BrokerGenericException.class)
    public ResponseEntity<String> handleException(BrokerGenericException ex) {
        log.error("An error occurred: {} {} {}", ex.getMessage(), ex.getCause(), Arrays.toString(ex.getStackTrace()));
        String errorMessage = "An error occurred: " + ex.getMessage();
        // Returned HTTP status codes for exceptions are managed from here
        HttpStatus httpStatus = switch (GenericExceptionMessages.fromMessage(ex.getMessage())) {
            case USERNAME_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case BAD_USERNAME_OR_PASSWORD, JWT_EXPIRED, NOT_AUTHORIZED_TO_PERFORM -> HttpStatus.UNAUTHORIZED;
            case CUSTOMER_NOT_FOUND, ROLE_NOT_FOUND, ORDER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case null, default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return new ResponseEntity<>(errorMessage, httpStatus);
    }

    /*
     * Access denied exception from @PreAuthorize handler
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleException(AccessDeniedException ex) {
        log.error("An error occurred: {} {} {}", ex.getMessage(), ex.getCause(), Arrays.toString(ex.getStackTrace()));
        return new ResponseEntity<>("Access denied. ", HttpStatus.FORBIDDEN);
    }

    /*
     * MethodArgumentNotValidException handler for invalid input parameters
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.error("Validation error: {}", errorMessage);
        return new ResponseEntity<>("Validation error: " + errorMessage, HttpStatus.BAD_REQUEST);
    }

    /*
     * IllegalArgumentException handler for invalid input parameters
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleException(IllegalArgumentException ex) {
        log.error("Invalid input parameter: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Used to catch IllegalArgumentException from persistence layer
    @ExceptionHandler({PersistenceException.class, DataAccessException.class})
    public ResponseEntity<String> handleException(Exception ex) {
        Throwable rootCause = NestedExceptionUtils.getRootCause(ex);
        if (rootCause instanceof IllegalArgumentException) {
            return handleException((IllegalArgumentException) rootCause);
        }
        log.error("Database error: {}", ex.getMessage());
        return handleUnexpectedException(ex);
    }

    /*
     * Unexpected exception handler
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpectedException(Exception ex) {
        log.error("An unexpected error occurred: {} {} {}", ex.getMessage(), ex.getCause(), Arrays.toString(ex.getStackTrace()));
        // ex.getMessage() for unexpected exceptions is not exposed on purpose
        return new ResponseEntity<>("An unexpected error occurred!", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
