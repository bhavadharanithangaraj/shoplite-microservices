package com.shoplite.inventory.exception;

// Becomes HTTP 404 (see GlobalExceptionHandler)
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Product " + id + " not found");
    }
}
