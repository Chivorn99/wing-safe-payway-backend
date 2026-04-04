package com.wingsafepay.wing_safe_pay.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetRequest {
    private String category;
    private BigDecimal limit;
    private String currency;
}
