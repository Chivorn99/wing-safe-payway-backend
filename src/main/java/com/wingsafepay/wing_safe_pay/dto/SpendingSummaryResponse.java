package com.wingsafepay.wing_safe_pay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingSummaryResponse {
    private BigDecimal totalSpent;
    private Long totalTransactions;
    private Long blockedTransactions;
    private Map<String, BigDecimal> categoryBreakdown;
}