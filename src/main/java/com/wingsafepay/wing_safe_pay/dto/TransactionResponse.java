package com.wingsafepay.wing_safe_pay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String recipientName;
    private String bankName;
    private BigDecimal amount;
    private String currency;
    private String category;
    private String riskLevel;
    private String paymentContext;
    private String status;
    private String note;
    private LocalDateTime createdAt;
}
