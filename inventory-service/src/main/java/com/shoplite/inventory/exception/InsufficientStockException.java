package com.shoplite.inventory.exception;

// Becomes HTTP 400 (see GlobalExceptionHandler)
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName, int requested, int available) {
        super("Not enough stock for " + productName + ": requested " + requested + ", available " + available);
    }
}
