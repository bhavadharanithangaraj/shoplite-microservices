package com.shoplite.order.service;

import com.shoplite.order.client.InventoryClient;
import com.shoplite.order.client.ProductDto;
import com.shoplite.order.dto.OrderRequest;
import com.shoplite.order.dto.OrderResponse;
import com.shoplite.order.entity.Order;
import com.shoplite.order.entity.OrderStatus;
import com.shoplite.order.exception.InsufficientStockException;
import com.shoplite.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public OrderService(OrderRepository orderRepository, InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
    }

    public OrderResponse placeOrder(OrderRequest request) {
        // 1. Ask inventory-service about the product (HTTP GET via Feign)
        ProductDto product = inventoryClient.getProduct(request.productId());

        // 2. Check stock before trying to reserve it, so we can give a clear message
        if (product.stock() < request.quantity()) {
            throw new InsufficientStockException("Not enough stock for " + product.name()
                    + ": requested " + request.quantity() + ", available " + product.stock());
        }

        // 3. Reduce the stock in inventory-service (HTTP PUT via Feign)
        inventoryClient.reduceStock(request.productId(), request.quantity());

        // 4. Save the order in our own database
        Order order = new Order(request.productId(), request.quantity(), OrderStatus.CONFIRMED);
        return OrderResponse.from(orderRepository.save(order));
    }

    public List<OrderResponse> getAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(OrderResponse::from)
                .toList();
    }
}
