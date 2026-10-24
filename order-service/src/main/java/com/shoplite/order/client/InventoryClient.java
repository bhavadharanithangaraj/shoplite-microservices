package com.shoplite.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// We only write the interface. Feign generates the code that sends the HTTP requests
// and turns the JSON response into a ProductDto. The annotations mirror inventory-service's controller.
@FeignClient(name = "inventory-service", url = "${inventory.service.url}")
public interface InventoryClient {

    // GET http://inventory-service/products/{id}
    @GetMapping("/products/{id}")
    ProductDto getProduct(@PathVariable("id") Long id);

    // PUT http://inventory-service/products/{id}/reduce?qty=...
    @PutMapping("/products/{id}/reduce")
    ProductDto reduceStock(@PathVariable("id") Long id, @RequestParam("qty") int qty);
}
