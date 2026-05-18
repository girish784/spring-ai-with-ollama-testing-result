package com.example.customersupportai.model;

public record SopSummary(
        String id,
        String filename,
        int wordCount
) {
    public static SopSummary from(SopDocument document) {
        return new SopSummary(document.id(), document.filename(), document.wordCount());
    }
}
