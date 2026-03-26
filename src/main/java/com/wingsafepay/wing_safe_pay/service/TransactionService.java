package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.wing_safe_pay.dto.TransactionDTO;
import com.wingsafepay.wing_safe_pay.model.Transaction;
import com.wingsafepay.wing_safe_pay.model.User;
import com.wingsafepay.wing_safe_pay.repository.TransactionRepository;
import com.wingsafepay.wing_safe_pay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionDTO save(TransactionDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction tx = Transaction.builder()
                .user(user)
                .recipientName(dto.getRecipientName())
                .recipientBank(dto.getRecipientBank())
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .riskLevel(dto.getRiskLevel())
                .category(dto.getCategory())
                .proceeded(dto.isProceeded())
                .build();

        transactionRepository.save(tx);
        dto.setCreatedAt(tx.getCreatedAt());
        return dto;
    }

    public List<TransactionDTO> getByUser(Long userId) {
        return transactionRepository.findByUserId(userId)
                .stream()
                .map(tx -> {
                    TransactionDTO dto = new TransactionDTO();
                    dto.setUserId(userId);
                    dto.setRecipientName(tx.getRecipientName());
                    dto.setRecipientBank(tx.getRecipientBank());
                    dto.setAmount(tx.getAmount());
                    dto.setCurrency(tx.getCurrency());
                    dto.setRiskLevel(tx.getRiskLevel());
                    dto.setCategory(tx.getCategory());
                    dto.setProceeded(tx.isProceeded());
                    dto.setCreatedAt(tx.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getCategorySummary(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        Map<String, Double> byCategory = transactions.stream()
                .filter(tx -> tx.getCategory() != null && tx.getAmount() != null)
                .collect(Collectors.groupingBy(
                        tx -> tx.getCategory().name(),
                        Collectors.summingDouble(tx -> tx.getAmount().doubleValue())
                ));

        long totalTx = transactions.size();
        long suspiciousCount = transactions.stream()
                .filter(tx -> tx.getRiskLevel() != null &&
                        tx.getRiskLevel().name().equals("HIGH_RISK"))
                .count();

        return Map.of(
                "byCategory", byCategory,
                "totalTransactions", totalTx,
                "suspiciousBlocked", suspiciousCount
        );
    }
}