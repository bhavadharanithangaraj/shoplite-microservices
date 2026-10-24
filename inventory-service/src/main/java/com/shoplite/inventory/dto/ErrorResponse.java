package com.shoplite.inventory.dto;

import java.time.Instant;

// Same error shape for every error, e.g.
// { "timestamp": "...", "status": 400, "error": "Bad Request", "message": "Not enough stock ..." }
public record ErrorResponse(Instant timestamp, int status, String error, String message) {

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(Instant.now(), status, error, message);
    }
}
