package com.wingsafepay.wing_safe_pay.dto;

import com.wingsafepay.wing_safe_pay.enums.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptScanResponse {
    private String recipientName;
    private String bankName;
    private BigDecimal amount;
    private String currency;
    private TransactionCategory category;
    private RiskLevel riskLevel;
    private PaymentContext paymentContext;
    private TransactionStatus status;
    private String note;
    private String rawText;
}