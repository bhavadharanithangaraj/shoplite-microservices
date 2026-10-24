package com.shoplite.order.exception;

// Not enough stock for the order. Becomes HTTP 400 (see GlobalExceptionHandler).
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
