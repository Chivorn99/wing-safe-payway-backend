package com.wingsafepay.wing_safe_pay.dto;

import com.wingsafepay.wing_safe_pay.enums.PaymentContext;
import com.wingsafepay.wing_safe_pay.enums.RiskLevel;
import com.wingsafepay.wing_safe_pay.enums.TransactionCategory;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class QRCheckResponse {
    private String recipientName;
    private String bankName;
    private BigDecimal amount;
    private String qrType;
    private PaymentContext paymentContext;
    private RiskLevel riskLevel;
    private TransactionCategory category;
    private List<String> warnings;
    private List<String> passedChecks;
    private String message;
}