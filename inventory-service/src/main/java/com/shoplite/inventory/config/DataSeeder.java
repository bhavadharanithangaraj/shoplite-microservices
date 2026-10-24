package com.shoplite.inventory.config;

import com.shoplite.inventory.entity.Product;
import com.shoplite.inventory.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// Runs once at startup. Adds sample products, but only if the table is empty.
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ProductRepository productRepository;

    public DataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            productRepository.saveAll(List.of(
                    new Product("Laptop", 10),
                    new Product("Smartphone", 25),
                    new Product("Headphones", 50),
                    new Product("Keyboard", 5)
            ));
            log.info("Seeded 4 sample products");
        }
    }
}
