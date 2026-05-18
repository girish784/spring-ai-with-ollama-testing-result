package com.example.customersupportai.model;

public record SopDocument(
        String id,
        String filename,
        String content,
        int wordCount
) {
}
