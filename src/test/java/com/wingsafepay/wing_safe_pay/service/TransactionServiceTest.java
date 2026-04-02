package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.wing_safe_pay.dto.TransactionDTO;
import com.wingsafepay.wing_safe_pay.dto.TransactionResponse;
import com.wingsafepay.wing_safe_pay.dto.SpendingSummaryResponse;
import com.wingsafepay.wing_safe_pay.enums.*;
import com.wingsafepay.wing_safe_pay.model.Transaction;
import com.wingsafepay.wing_safe_pay.model.User;
import com.wingsafepay.wing_safe_pay.repository.TransactionRepository;
import com.wingsafepay.wing_safe_pay.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private TransactionService transactionService;

    private User testUser() {
        return User.builder().id(1L).phoneNumber("012345678")
                .fullName("Test").password("x").role(Role.USER).build();
    }

    private Transaction sampleTx(User user) {
        return Transaction.builder()
                .id(1L).user(user)
                .recipientName("Coffee Shop").bankName("ABA Bank")
                .amount(new BigDecimal("5.00")).currency("USD")
                .category(TransactionCategory.FOOD).riskLevel(RiskLevel.SAFE)
                .paymentContext(PaymentContext.MERCHANT).status(TransactionStatus.PAID)
                .note("Morning coffee").createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void saveTransaction_success() {
        User user = testUser();
        TransactionDTO dto = new TransactionDTO();
        dto.setRecipientName("Coffee Shop");
        dto.setBankName("ABA Bank");
        dto.setAmount(new BigDecimal("5.00"));
        dto.setCurrency("USD");
        dto.setCategory(TransactionCategory.FOOD);
        dto.setRiskLevel(RiskLevel.SAFE);
        dto.setPaymentContext(PaymentContext.MERCHANT);
        dto.setStatus(TransactionStatus.PAID);
        dto.setNote("Morning coffee");

        when(userRepository.findByPhoneNumber("012345678")).thenReturn(Optional.of(user));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction tx = i.getArgument(0);
            tx.setId(1L);
            tx.setCreatedAt(LocalDateTime.now());
            return tx;
        });

        TransactionResponse resp = transactionService.saveTransaction("012345678", dto);

        assertEquals("Coffee Shop", resp.getRecipientName());
        assertEquals("FOOD", resp.getCategory());
        verify(transactionRepository).save(any());
    }

    @Test
    void getSummary_calculatesCorrectly() {
        User user = testUser();
        Transaction paid1 = sampleTx(user);
        Transaction paid2 = Transaction.builder()
                .id(2L).user(user).recipientName("Grab").bankName("Wing Bank")
                .amount(new BigDecimal("3.00")).currency("USD")
                .category(TransactionCategory.TRANSPORT).riskLevel(RiskLevel.SAFE)
                .paymentContext(PaymentContext.MERCHANT).status(TransactionStatus.PAID)
                .note("Ride").createdAt(LocalDateTime.now()).build();
        Transaction blocked = Transaction.builder()
                .id(3L).user(user).recipientName("Sus").bankName("Unknown")
                .amount(new BigDecimal("500")).currency("USD")
                .category(TransactionCategory.OTHER).riskLevel(RiskLevel.HIGH_RISK)
                .paymentContext(PaymentContext.MERCHANT).status(TransactionStatus.BLOCKED)
                .note("Blocked").createdAt(LocalDateTime.now()).build();

        when(userRepository.findByPhoneNumber("012345678")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(paid1, paid2, blocked));
        when(transactionRepository.countByUserAndStatus(user, TransactionStatus.BLOCKED)).thenReturn(1L);

        SpendingSummaryResponse summary = transactionService.getSummary("012345678");

        // Only PAID transactions count toward totalSpent
        assertEquals(0, summary.getTotalSpent().compareTo(new BigDecimal("8.00")));
        assertEquals(3L, summary.getTotalTransactions());
        assertEquals(1L, summary.getBlockedTransactions());
        assertEquals(2, summary.getCategoryBreakdown().size());
    }

    @Test
    void searchTransactions_returnsResults() {
        Transaction tx = sampleTx(testUser());

        when(transactionRepository.searchByKeyword("012345678", "coffee")).thenReturn(List.of(tx));

        List<TransactionResponse> results = transactionService.searchTransactions("012345678", "coffee");

        assertEquals(1, results.size());
        assertEquals("Coffee Shop", results.get(0).getRecipientName());
    }
}
