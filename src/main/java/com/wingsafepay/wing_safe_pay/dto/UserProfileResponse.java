package com.wingsafepay.wing_safe_pay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String fullName;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private long totalTransactions;
    private String profileImage;
}