package com.example.customersupportai.service;

import com.example.customersupportai.model.PlanResponse;
import com.example.customersupportai.model.RetrievedSop;
import com.example.customersupportai.model.RoutingDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanningService {

    private static final Logger log = LoggerFactory.getLogger(PlanningService.class);

    private final RouterService routerService;
    private final Bm25Retriever bm25Retriever;
    private final ChatClient chatClient;

    public PlanningService(
            RouterService routerService,
            Bm25Retriever bm25Retriever,
            ChatClient.Builder chatClientBuilder
    ) {
        this.routerService = routerService;
        this.bm25Retriever = bm25Retriever;
        this.chatClient = chatClientBuilder.build();
    }

    public PlanResponse plan(String customerMessage) {
        log.info("Planning started messageLength={}", customerMessage.length());
        RoutingDecision decision = routerService.route(customerMessage);
        List<RetrievedSop> retrievedSops = bm25Retriever.retrieve(customerMessage, decision.department());
        log.info("Retrieved {} SOPs for department={}", retrievedSops.size(), decision.department());
        String plan = generatePlan(customerMessage, decision, retrievedSops);
        log.info("Planning completed department={} planLength={}", decision.department(), plan.length());
        return new PlanResponse(customerMessage, decision.department(), retrievedSops, plan);
    }

    private String generatePlan(String customerMessage, RoutingDecision decision, List<RetrievedSop> retrievedSops) {
        String sopsContext = retrievedSops.stream()
                .map(sop -> "--- " + sop.sopId() + " (Relevance: " + String.format("%.2f", sop.score()) + ") ---\n"
                        + limit(sop.excerpt(), 2_000))
                .reduce("", (left, right) -> left + "\n\n" + right);

        String prompt = """
                You are a customer support agent planning assistant. Create a detailed, step-by-step action plan.

                Customer Message:
                "%s"

                Department: %s

                Relevant Procedures:
                %s

                Instructions:
                Create a detailed action plan that:
                1. Lists specific steps the agent should take in order
                2. References relevant SOP procedures
                3. Includes verification or security steps
                4. Mentions escalation criteria if applicable
                5. Provides timeline expectations
                6. Notes edge cases or system limitations

                Format as a numbered action plan. Be specific and actionable.
                """.formatted(customerMessage, decision.department().name(), sopsContext);

        log.debug("Calling planning model with {} retrieved SOP excerpts", retrievedSops.size());
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    private String limit(String value, int maxChars) {
        if (value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, maxChars) + "...";
    }
}
