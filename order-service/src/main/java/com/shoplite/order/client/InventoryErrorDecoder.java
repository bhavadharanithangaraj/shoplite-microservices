package com.shoplite.order.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoplite.order.exception.InsufficientStockException;
import com.shoplite.order.exception.ProductNotFoundException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Feign calls this whenever inventory-service answers with an error status (4xx/5xx).
// We turn those HTTP errors into our own exceptions, keeping inventory-service's message.
@Component
public class InventoryErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();
    private final ObjectMapper objectMapper;

    public InventoryErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        String message = readMessage(response);

        return switch (response.status()) {
            case 404 -> new ProductNotFoundException(message != null ? message : "Product not found");
            // inventory-service only returns 400 from reduce when stock ran out
            // (e.g. another order took it between our check and our reduce call)
            case 400 -> new InsufficientStockException(message != null ? message : "Not enough stock");
            default -> defaultDecoder.decode(methodKey, response);
        };
    }

    // inventory-service sends { ..., "message": "..." } - pull out the message
    private String readMessage(Response response) {
        if (response.body() == null) {
            return null;
        }
        try {
            String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            JsonNode json = objectMapper.readTree(body);
            return json.hasNonNull("message") ? json.get("message").asText() : null;
        } catch (IOException e) {
            return null;
        }
    }
}
