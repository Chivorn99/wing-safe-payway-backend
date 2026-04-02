package com.wingsafepay.wing_safe_pay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SavingGoalRequest {
    @NotBlank(message = "Goal title is required")
    private String title;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be greater than zero")
    private BigDecimal targetAmount;

    private LocalDate deadline;

    private String currency;

    private String emoji;
}