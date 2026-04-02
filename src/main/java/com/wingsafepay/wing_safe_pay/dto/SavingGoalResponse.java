package com.wingsafepay.wing_safe_pay.dto;

import com.wingsafepay.wing_safe_pay.enums.SavingGoalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingGoalResponse {
    private Long id;
    private String title;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal progressPercent;
    private LocalDate deadline;
    private SavingGoalStatus status;
    private String currency;
    private String emoji;
    private LocalDateTime createdAt;
}