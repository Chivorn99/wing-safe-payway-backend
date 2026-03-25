package com.wingsafepay.wing_safe_pay.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class QRCheckRequest {
    private String merchantId;
    private String displayedName;
    private String bankName;
    private BigDecimal amount;
    private String currency;
    private String qrType;   // "MERCHANT" or "PERSONAL"
}