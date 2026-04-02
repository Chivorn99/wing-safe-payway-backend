package com.wingsafepay.wing_safe_pay.controller;

import com.wingsafepay.wing_safe_pay.dto.SpendingSummaryResponse;
import com.wingsafepay.wing_safe_pay.dto.TransactionDTO;
import com.wingsafepay.wing_safe_pay.dto.TransactionResponse;
import com.wingsafepay.wing_safe_pay.enums.TransactionCategory;
import com.wingsafepay.wing_safe_pay.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> saveTransaction(
            Authentication authentication,
            @Valid @RequestBody TransactionDTO dto
    ) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(transactionService.saveTransaction(phoneNumber, dto));
    }

    @GetMapping("/me")
    public ResponseEntity<List<TransactionResponse>> getMyTransactions(Authentication authentication) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(transactionService.getUserTransactions(phoneNumber));
    }

    @GetMapping("/summary")
    public ResponseEntity<SpendingSummaryResponse> getSummary(Authentication authentication) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(transactionService.getSummary(phoneNumber));
    }

    /** Filter transactions by category — uses explicit JOIN query */
    @GetMapping("/filter")
    public ResponseEntity<List<TransactionResponse>> filterByCategory(
            Authentication authentication,
            @RequestParam TransactionCategory category
    ) {
        return ResponseEntity.ok(transactionService.filterByCategory(authentication.getName(), category));
    }

    /** Search transactions by keyword — uses explicit JOIN query */
    @GetMapping("/search")
    public ResponseEntity<List<TransactionResponse>> searchTransactions(
            Authentication authentication,
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(transactionService.searchTransactions(authentication.getName(), keyword));
    }
}