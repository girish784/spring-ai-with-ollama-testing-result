package com.example.customersupportai.service;

import com.example.customersupportai.model.Department;
import com.example.customersupportai.model.RoutingDecision;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class RouterService {

    private static final Logger log = LoggerFactory.getLogger(RouterService.class);

    private static final String SYSTEM_PROMPT = """
            Route customer messages to departments.

            Available departments:
            - BILLING: Payment issues, charges, refunds, refund status, account balances, fees
            - RETURNS: Return requests, exchanges, return status, return policies
            - TECHNICAL_SUPPORT: Login problems, password reset issues, website errors, checkout failures
            - ORDER_STATUS: Order tracking, shipping updates, delivery questions, missing items
            - PRODUCT_INQUIRY: Product questions, specifications, availability, pricing
            - ACCOUNT_MANAGEMENT: Profile updates, changing saved payment methods, preferences, address changes
            - ESCALATION: Very upset customers demanding managers, supervisor requests

            Important:
            - Login/password problems = TECHNICAL_SUPPORT (not ACCOUNT_MANAGEMENT)
            - Updating payment methods = ACCOUNT_MANAGEMENT (not BILLING)
            - Refund status = BILLING (not RETURNS)

            Respond with JSON:
            {
              "department": "DEPARTMENT_NAME",
              "reasoning": "Your reasoning"
            }
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public RouterService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public RoutingDecision route(String customerMessage) {
        log.debug("Routing customer message with length={}", customerMessage.length());
        String content = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(customerMessage)
                .call()
                .content();

        RoutingDecision decision = parseDecision(customerMessage, content);
        log.info("Routing completed department={}", decision.department());
        return decision;
    }

    private RoutingDecision parseDecision(String customerMessage, String content) {
        try {
            JsonNode json = objectMapper.readTree(content);
            Department department = Department.fromModelValue(json.path("department").asText());
            String reasoning = json.path("reasoning").asText("No reasoning provided");
            return new RoutingDecision(department, reasoning, customerMessage);
        } catch (Exception ex) {
            return new RoutingDecision(
                    Department.fromModelValue(content),
                    "Model response could not be parsed as JSON.",
                    customerMessage
            );
        }
    }
}
