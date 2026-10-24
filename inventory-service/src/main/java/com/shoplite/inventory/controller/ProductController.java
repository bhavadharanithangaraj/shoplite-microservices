package com.shoplite.inventory.controller;

import com.shoplite.inventory.dto.ProductRequest;
import com.shoplite.inventory.dto.ProductResponse;
import com.shoplite.inventory.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    // PUT /products/1/reduce?qty=2  -> takes 2 units out of stock
    @PutMapping("/{id}/reduce")
    public ProductResponse reduceStock(@PathVariable Long id, @RequestParam int qty) {
        return productService.reduceStock(id, qty);
    }
}
