package com.wingsafepay.wing_safe_pay.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String phoneNumber;
    private String password;
    private String fullName;
}