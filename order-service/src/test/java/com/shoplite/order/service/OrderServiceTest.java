package com.shoplite.order.service;

import com.shoplite.order.client.InventoryClient;
import com.shoplite.order.client.ProductDto;
import com.shoplite.order.dto.OrderRequest;
import com.shoplite.order.dto.OrderResponse;
import com.shoplite.order.entity.Order;
import com.shoplite.order.entity.OrderStatus;
import com.shoplite.order.exception.InsufficientStockException;
import com.shoplite.order.exception.ProductNotFoundException;
import com.shoplite.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// Unit tests: the Feign client and the repository are Mockito mocks,
// so no inventory-service and no database are needed.
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private InventoryClient inventoryClient; // fake inventory-service

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_withEnoughStock_reducesStockAndSavesConfirmedOrder() {
        // inventory-service says: Laptop has 10 in stock
        when(inventoryClient.getProduct(1L)).thenReturn(new ProductDto(1L, "Laptop", 10));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.placeOrder(new OrderRequest(1L, 3));

        verify(inventoryClient).reduceStock(1L, 3); // stock was reduced by exactly 3
        assertThat(response.productId()).isEqualTo(1L);
        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void placeOrder_withNotEnoughStock_throwsAndChangesNothing() {
        // inventory-service says: only 2 Keyboards left
        when(inventoryClient.getProduct(4L)).thenReturn(new ProductDto(4L, "Keyboard", 2));

        assertThatThrownBy(() -> orderService.placeOrder(new OrderRequest(4L, 5)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Not enough stock for Keyboard: requested 5, available 2");

        // Nothing should happen: no stock reduced, no order saved
        verify(inventoryClient, never()).reduceStock(anyLong(), anyInt());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_whenProductDoesNotExist_savesNothing() {
        // What InventoryErrorDecoder throws when inventory-service answers 404
        when(inventoryClient.getProduct(999L)).thenThrow(new ProductNotFoundException("Product 999 not found"));

        assertThatThrownBy(() -> orderService.placeOrder(new OrderRequest(999L, 1)))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product 999 not found");

        verify(orderRepository, never()).save(any());
    }
}
