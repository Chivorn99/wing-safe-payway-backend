package com.wingsafepay.wing_safe_pay.model;

import com.wingsafepay.enums.RiskLevel;
import com.wingsafepay.enums.TransactionCategory;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String recipientName;
    private String recipientBank;
    private BigDecimal amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;       // SAFE, WARNING, HIGH_RISK

    @Enumerated(EnumType.STRING)
    private TransactionCategory category;  // FOOD, SHOPPING, TRANSPORT, etc.

    private boolean proceeded;  // did the user pay after seeing the warning?

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}