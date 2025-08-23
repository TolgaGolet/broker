package com.brokagefirm.broker.exception;

import com.brokagefirm.broker.exception.enums.GenericExceptionMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;

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
            case BAD_USERNAME_OR_PASSWORD, JWT_EXPIRED -> HttpStatus.UNAUTHORIZED;
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
     * Unexpected exception handler
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("An unexpected error occurred: {} {} {}", ex.getMessage(), ex.getCause(), Arrays.toString(ex.getStackTrace()));
        // ex.getMessage() for unexpected exceptions is not exposed on purpose
        return new ResponseEntity<>("An unexpected error occurred!", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
