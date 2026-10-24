package com.shoplite.order.exception;

// Thrown when the product doesn't have enough stock for the order.
// Step 3 turns this into a clear HTTP 400 via @ControllerAdvice.
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
