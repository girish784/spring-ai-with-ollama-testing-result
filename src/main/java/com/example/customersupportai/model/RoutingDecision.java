package com.example.customersupportai.model;

public record RoutingDecision(
        Department department,
        String reasoning,
        String customerMessage
) {
}
