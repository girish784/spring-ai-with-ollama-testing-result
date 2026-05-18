package com.example.customersupportai.web;

import com.example.customersupportai.model.PlanRequest;
import com.example.customersupportai.model.PlanResponse;
import com.example.customersupportai.model.RouteRequest;
import com.example.customersupportai.model.RoutingDecision;
import com.example.customersupportai.model.SopSummary;
import com.example.customersupportai.service.PlanningService;
import com.example.customersupportai.service.RouterService;
import com.example.customersupportai.service.SopRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Customer Support AI", description = "Routing and SOP-based planning endpoints")
public class SupportController {

    private final RouterService routerService;
    private final PlanningService planningService;
    private final SopRepository sopRepository;

    public SupportController(
            RouterService routerService,
            PlanningService planningService,
            SopRepository sopRepository
    ) {
        this.routerService = routerService;
        this.planningService = planningService;
        this.sopRepository = sopRepository;
    }

    @GetMapping("/health")
    @Operation(summary = "Check API health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/v1/route")
    @Operation(summary = "Route a customer message to a support department")
    public RoutingDecision route(@Valid @RequestBody RouteRequest request) {
        return routerService.route(request.message());
    }

    @PostMapping("/v2/plan")
    @Operation(summary = "Create an SOP-based support action plan")
    public PlanResponse plan(@Valid @RequestBody PlanRequest request) {
        return planningService.plan(request.message());
    }

    @GetMapping("/sops")
    @Operation(summary = "List loaded SOP documents")
    public List<SopSummary> sops() {
        return sopRepository.findAll().stream()
                .map(SopSummary::from)
                .toList();
    }
}
