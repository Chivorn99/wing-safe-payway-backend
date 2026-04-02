package com.wingsafepay.wing_safe_pay.controller;

import com.wingsafepay.wing_safe_pay.dto.GoalProgressRequest;
import com.wingsafepay.wing_safe_pay.dto.SavingGoalRequest;
import com.wingsafepay.wing_safe_pay.dto.SavingGoalResponse;
import com.wingsafepay.wing_safe_pay.service.SavingGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class SavingGoalController {

    private final SavingGoalService savingGoalService;

    @PostMapping
    public ResponseEntity<SavingGoalResponse> createGoal(
            Authentication authentication,
            @Valid @RequestBody SavingGoalRequest request
    ) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(savingGoalService.create(phoneNumber, request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<SavingGoalResponse>> getMyGoals(Authentication authentication) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(savingGoalService.getUserGoals(phoneNumber));
    }

    @PatchMapping("/{goalId}/progress")
    public ResponseEntity<SavingGoalResponse> addProgress(
            Authentication authentication,
            @PathVariable Long goalId,
            @Valid @RequestBody GoalProgressRequest request
    ) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(savingGoalService.addProgress(phoneNumber, goalId, request));
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteGoal(
            Authentication authentication,
            @PathVariable Long goalId
    ) {
        String phoneNumber = authentication.getName();
        savingGoalService.deleteGoal(phoneNumber, goalId);
        return ResponseEntity.noContent().build();
    }
}