package com.example.customersupportai.model;

import jakarta.validation.constraints.NotBlank;

public record RouteRequest(@NotBlank String message) {
}
