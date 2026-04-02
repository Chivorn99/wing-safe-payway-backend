package com.wingsafepay.wing_safe_pay.model;

import com.wingsafepay.wing_safe_pay.enums.SavingGoalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "saving_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentAmount;

    private LocalDate deadline;

    @Column(length = 10)
    private String currency;

    @Column(length = 10)
    private String emoji;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SavingGoalStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.currentAmount == null) this.currentAmount = BigDecimal.ZERO;
        if (this.status == null) this.status = SavingGoalStatus.ACTIVE;
        if (this.currency == null) this.currency = "USD";
        if (this.emoji == null) this.emoji = "🎯";
    }
}