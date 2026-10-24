package com.shoplite.order.controller;

import com.shoplite.order.exception.InsufficientStockException;
import com.shoplite.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Starts only the web layer (controller + @ControllerAdvice) and sends a fake HTTP request,
// to prove the client really gets a clear 400 JSON error when stock is too low.
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void placeOrder_withNotEnoughStock_returns400WithClearMessage() throws Exception {
        when(orderService.placeOrder(any()))
                .thenThrow(new InsufficientStockException("Not enough stock for Keyboard: requested 5, available 2"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": 4, \"quantity\": 5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Not enough stock for Keyboard: requested 5, available 2"));
    }
}
