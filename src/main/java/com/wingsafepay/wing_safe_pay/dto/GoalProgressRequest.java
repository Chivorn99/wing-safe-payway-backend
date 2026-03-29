package com.wingsafepay.wing_safe_pay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoalProgressRequest {
    private BigDecimal amount;
}