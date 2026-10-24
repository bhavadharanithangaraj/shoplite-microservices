package com.shoplite.order.client;

// order-service's own copy of the product JSON that inventory-service returns.
// Services don't share Java classes - they only agree on the JSON format.
public record ProductDto(Long id, String name, int stock) {
}
