package com.wingsafepay.wing_safe_pay.controller;

import com.wingsafepay.wing_safe_pay.dto.BudgetRequest;
import com.wingsafepay.wing_safe_pay.dto.BudgetResponse;
import com.wingsafepay.wing_safe_pay.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgets(Authentication auth) {
        return ResponseEntity.ok(budgetService.getBudgets(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> setBudget(
            Authentication auth,
            @RequestBody BudgetRequest request
    ) {
        return ResponseEntity.ok(budgetService.setBudget(auth.getName(), request));
    }

    @DeleteMapping("/{category}")
    public ResponseEntity<Void> removeBudget(
            Authentication auth,
            @PathVariable String category
    ) {
        budgetService.removeBudget(auth.getName(), category);
        return ResponseEntity.noContent().build();
    }
}
