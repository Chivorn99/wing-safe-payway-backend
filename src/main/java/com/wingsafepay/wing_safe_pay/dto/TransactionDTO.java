package com.wingsafepay.wing_safe_pay.dto;

import com.wingsafepay.wing_safe_pay.enums.PaymentContext;
import com.wingsafepay.wing_safe_pay.enums.RiskLevel;
import com.wingsafepay.wing_safe_pay.enums.TransactionCategory;
import com.wingsafepay.wing_safe_pay.enums.TransactionStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionDTO {
    private String merchantId;
    private String recipientName;
    private String bankName;
    private BigDecimal amount;
    private String currency;
    private TransactionCategory category;
    private RiskLevel riskLevel;
    private PaymentContext paymentContext;
    private TransactionStatus status;
    private String note;
}