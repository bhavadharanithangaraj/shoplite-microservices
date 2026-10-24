package com.shoplite.order.entity;

// Only confirmed orders are saved for now; statuses like CANCELLED or SHIPPED could be added later.
public enum OrderStatus {
    CONFIRMED
}
