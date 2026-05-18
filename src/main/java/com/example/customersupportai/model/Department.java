package com.example.customersupportai.model;

public enum Department {
    BILLING,
    RETURNS,
    TECHNICAL_SUPPORT,
    ORDER_STATUS,
    PRODUCT_INQUIRY,
    ACCOUNT_MANAGEMENT,
    ESCALATION;

    public static Department fromModelValue(String value) {
        if (value == null || value.isBlank()) {
            return ESCALATION;
        }
        String normalized = value.trim()
                .replace("\"", "")
                .replace("-", "_")
                .replace(" ", "_")
                .toUpperCase();
        try {
            return Department.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return ESCALATION;
        }
    }
}
