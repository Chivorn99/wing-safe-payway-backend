package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.wing_safe_pay.dto.SpendingSummaryResponse;
import com.wingsafepay.wing_safe_pay.dto.TransactionDTO;
import com.wingsafepay.wing_safe_pay.dto.TransactionResponse;
import com.wingsafepay.wing_safe_pay.enums.TransactionStatus;
import com.wingsafepay.wing_safe_pay.exception.NotFoundException;
import com.wingsafepay.wing_safe_pay.model.Merchant;
import com.wingsafepay.wing_safe_pay.model.Transaction;
import com.wingsafepay.wing_safe_pay.model.User;
import com.wingsafepay.wing_safe_pay.repository.MerchantRepository;
import com.wingsafepay.wing_safe_pay.repository.TransactionRepository;
import com.wingsafepay.wing_safe_pay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;

    public TransactionResponse saveTransaction(String phoneNumber, TransactionDTO dto) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Transaction tx = Transaction.builder()
                .user(user)
                .recipientName(dto.getRecipientName())
                .bankName(dto.getBankName())
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .category(dto.getCategory())
                .riskLevel(dto.getRiskLevel())
                .paymentContext(dto.getPaymentContext())
                .status(dto.getStatus())
                .note(dto.getNote())
                .build();

        if (dto.getMerchantId() != null && !dto.getMerchantId().isBlank()) {
            Merchant merchant = merchantRepository.findByMerchantId(dto.getMerchantId()).orElse(null);
            tx.setMerchant(merchant);
        }

        return toResponse(transactionRepository.save(tx));
    }

    public List<TransactionResponse> getUserTransactions(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return transactionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SpendingSummaryResponse getSummary(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<Transaction> transactions = transactionRepository.findByUserOrderByCreatedAtDesc(user);

        BigDecimal totalSpent = transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.PAID)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryBreakdown = new LinkedHashMap<>();
        transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.PAID)
                .forEach(t -> categoryBreakdown.merge(
                        t.getCategory().name(),
                        t.getAmount(),
                        BigDecimal::add
                ));

        long blockedCount = transactionRepository.countByUserAndStatus(user, TransactionStatus.BLOCKED);

        return SpendingSummaryResponse.builder()
                .totalSpent(totalSpent)
                .totalTransactions((long) transactions.size())
                .blockedTransactions(blockedCount)
                .categoryBreakdown(categoryBreakdown)
                .build();
    }

    private TransactionResponse toResponse(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .recipientName(tx.getRecipientName())
                .bankName(tx.getBankName())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .category(tx.getCategory().name())
                .riskLevel(tx.getRiskLevel().name())
                .paymentContext(tx.getPaymentContext().name())
                .status(tx.getStatus().name())
                .note(tx.getNote())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}