package com.shoplite.inventory.dto;

import com.shoplite.inventory.entity.Product;

// JSON we send back for a product
public record ProductResponse(Long id, String name, int stock) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getStock());
    }
}
