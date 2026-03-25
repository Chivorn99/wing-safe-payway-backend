package com.wingsafepay.wing_safe_pay.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "merchants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String merchantId;   // from KHQR payload

    @Column(nullable = false)
    private String merchantName;

    private String bankName;
    private String category;     // food, retail, transport, etc.
    private String province;
    private boolean isVerified;
}