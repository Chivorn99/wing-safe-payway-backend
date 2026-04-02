package com.wingsafepay.wing_safe_pay.dto;

import com.wingsafepay.wing_safe_pay.enums.PaymentContext;
import com.wingsafepay.wing_safe_pay.enums.RiskLevel;
import com.wingsafepay.wing_safe_pay.enums.TransactionCategory;
import com.wingsafepay.wing_safe_pay.enums.TransactionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionDTO {
    private String merchantId;

    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Category is required")
    private TransactionCategory category;

    @NotNull(message = "Risk level is required")
    private RiskLevel riskLevel;

    @NotNull(message = "Payment context is required")
    private PaymentContext paymentContext;

    @NotNull(message = "Status is required")
    private TransactionStatus status;

    private String note;
}