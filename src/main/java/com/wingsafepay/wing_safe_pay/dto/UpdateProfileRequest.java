package com.wingsafepay.wing_safe_pay.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phoneNumber;
    private String profileImage;
}
