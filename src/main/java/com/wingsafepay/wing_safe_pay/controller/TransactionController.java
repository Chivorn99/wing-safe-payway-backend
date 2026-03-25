package com.wingsafepay.wing_safe_pay.controller;

import com.wingsafepay.wing_safe_pay.dto.TransactionDTO;
import com.wingsafepay.wing_safe_pay.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionDTO> save(@RequestBody TransactionDTO dto) {
        return ResponseEntity.ok(transactionService.save(dto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionDTO>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getByUser(userId));
    }

    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<?> getSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getCategorySummary(userId));
    }
}
