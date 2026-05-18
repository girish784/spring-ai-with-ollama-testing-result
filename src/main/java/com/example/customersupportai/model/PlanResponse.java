package com.example.customersupportai.model;

import java.util.List;

public record PlanResponse(
        String message,
        Department department,
        List<RetrievedSop> retrievedSops,
        String plan
) {
}
