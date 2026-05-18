package com.example.customersupportai.model;

import jakarta.validation.constraints.NotBlank;

public record PlanRequest(@NotBlank String message) {
}
