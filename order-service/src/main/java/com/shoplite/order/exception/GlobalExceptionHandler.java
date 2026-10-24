package com.shoplite.order.exception;

import com.shoplite.order.dto.ErrorResponse;
import feign.FeignException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Catches exceptions from any controller and returns a clear JSON error with the right status code.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // e.g. "Not enough stock for Headphones: requested 100, available 49"
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // inventory-service is down or too slow (connection refused / timeout)
    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<ErrorResponse> handleInventoryDown(RetryableException ex) {
        log.warn("inventory-service unreachable: {}", ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Inventory service is unavailable. Please try again later.");
    }

    // inventory-service answered with an unexpected error (e.g. 500)
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleInventoryError(FeignException ex) {
        log.error("inventory-service returned {}", ex.status(), ex);
        return build(HttpStatus.BAD_GATEWAY, "Inventory service returned an error. Please try again later.");
    }

    // @Valid failed, e.g. "quantity must be at least 1"
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, message);
    }

    // Broken JSON or wrong types, e.g. "quantity": "abc"
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Request body is invalid or has a wrong value");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex) {
        // Spring's own errors (unknown URL = 404, wrong HTTP method = 405) keep their status
        if (ex instanceof org.springframework.web.ErrorResponse springError) {
            HttpStatus status = HttpStatus.valueOf(springError.getStatusCode().value());
            return build(status, status.getReasonPhrase());
        }
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status.value(), status.getReasonPhrase(), message));
    }
}
