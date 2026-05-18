package com.example.customersupportai.service;

import com.example.customersupportai.model.Department;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentTest {

    @Test
    void normalizesModelValues() {
        assertThat(Department.fromModelValue("technical support")).isEqualTo(Department.TECHNICAL_SUPPORT);
        assertThat(Department.fromModelValue("ORDER-STATUS")).isEqualTo(Department.ORDER_STATUS);
    }

    @Test
    void fallsBackToEscalationForUnknownValues() {
        assertThat(Department.fromModelValue("unknown")).isEqualTo(Department.ESCALATION);
        assertThat(Department.fromModelValue(null)).isEqualTo(Department.ESCALATION);
    }
}
