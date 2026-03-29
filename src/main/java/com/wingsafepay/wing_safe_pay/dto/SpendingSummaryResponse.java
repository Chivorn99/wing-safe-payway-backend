package com.wingsafepay.wing_safe_pay.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
public class SpendingSummaryResponse {
    private BigDecimal totalSpent;
    private Long totalTransactions;
    private Long blockedTransactions;
    private Map<String, BigDecimal> categoryBreakdown;
}