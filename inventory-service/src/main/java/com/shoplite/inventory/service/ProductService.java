package com.shoplite.inventory.service;

import com.shoplite.inventory.dto.ProductRequest;
import com.shoplite.inventory.dto.ProductResponse;
import com.shoplite.inventory.entity.Product;
import com.shoplite.inventory.exception.InsufficientStockException;
import com.shoplite.inventory.exception.ProductNotFoundException;
import com.shoplite.inventory.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll(Sort.by("id")).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return ProductResponse.from(findProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product(request.name().trim(), request.stock());
        return ProductResponse.from(productRepository.save(product));
    }

    // Called by order-service when an order is placed
    @Transactional
    public ProductResponse reduceStock(Long id, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        findProduct(id); // 404 if the product doesn't exist

        int updatedRows = productRepository.reduceStock(id, qty);

        // Read again: we need the stock as it is NOW (another order may have changed it)
        Product current = findProduct(id);
        if (updatedRows == 0) {
            throw new InsufficientStockException(current.getName(), qty, current.getStock());
        }
        return ProductResponse.from(current);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }
}
