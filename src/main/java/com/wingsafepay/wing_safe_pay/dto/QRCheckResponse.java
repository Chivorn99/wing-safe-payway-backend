package com.wingsafepay.wing_safe_pay.dto;

import com.wingsafepay.enums.RiskLevel;
import com.wingsafepay.enums.TransactionCategory;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class QRCheckResponse {
    private String recipientName;
    private String bankName;
    private BigDecimal amount;
    private String qrType;
    private RiskLevel riskLevel;        // SAFE, WARNING, HIGH_RISK
    private TransactionCategory category;
    private List<String> warnings;
    private List<String> passedChecks;
    private String message;
}
