package com.wingsafepay.wing_safe_pay.dto;

import com.wingsafepay.wing_safe_pay.enums.RiskLevel;
import com.wingsafepay.wing_safe_pay.enums.TransactionCategory;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long userId;
    private String recipientName;
    private String recipientBank;
    private BigDecimal amount;
    private String currency;
    private RiskLevel riskLevel;
    private TransactionCategory category;
    private boolean proceeded;
    private LocalDateTime createdAt;
}