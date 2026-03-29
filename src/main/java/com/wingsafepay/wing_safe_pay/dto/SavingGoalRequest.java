package com.wingsafepay.wing_safe_pay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SavingGoalRequest {
    private String title;
    private BigDecimal targetAmount;
    private LocalDate deadline;
}