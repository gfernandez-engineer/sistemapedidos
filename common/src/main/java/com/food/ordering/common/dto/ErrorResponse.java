package com.food.ordering.common.dto;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String title,
        String detail,
        String instance,
        Instant timestamp
) {
    public ErrorResponse(int status, String title, String detail, String instance) {
        this(status, title, detail, instance, Instant.now());
    }
}
