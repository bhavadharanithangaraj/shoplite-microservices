package com.shoplite.order.exception;

// inventory-service said the product doesn't exist. Becomes HTTP 404.
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }
}
