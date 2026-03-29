package com.wingsafepay.wing_safe_pay.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GoalProgressRequest {
    private BigDecimal amount;
}