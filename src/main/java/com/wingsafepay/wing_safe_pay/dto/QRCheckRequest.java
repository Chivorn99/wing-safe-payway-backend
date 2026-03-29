package com.wingsafepay.wing_safe_pay.dto;

import com.wingsafepay.wing_safe_pay.enums.PaymentContext;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class QRCheckRequest {
    private String merchantId;
    private String displayedName;
    private String bankName;
    private BigDecimal amount;
    private String currency;
    private String qrType;
    private PaymentContext paymentContext;
}