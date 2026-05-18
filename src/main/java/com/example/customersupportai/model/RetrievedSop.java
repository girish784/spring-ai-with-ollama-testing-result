package com.example.customersupportai.model;

public record RetrievedSop(
        String sopId,
        String filename,
        double score,
        String excerpt
) {
}
