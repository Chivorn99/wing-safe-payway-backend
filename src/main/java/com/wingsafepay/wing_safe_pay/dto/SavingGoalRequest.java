package com.wingsafepay.wing_safe_pay.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SavingGoalRequest {
    private String title;
    private BigDecimal targetAmount;
    private LocalDate deadline;
}