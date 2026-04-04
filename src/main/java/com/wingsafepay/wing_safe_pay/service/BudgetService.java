package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.wing_safe_pay.dto.BudgetRequest;
import com.wingsafepay.wing_safe_pay.dto.BudgetResponse;
import com.wingsafepay.wing_safe_pay.exception.NotFoundException;
import com.wingsafepay.wing_safe_pay.model.Budget;
import com.wingsafepay.wing_safe_pay.model.User;
import com.wingsafepay.wing_safe_pay.repository.BudgetRepository;
import com.wingsafepay.wing_safe_pay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    public List<BudgetResponse> getBudgets(String phoneNumber) {
        User user = findUser(phoneNumber);
        return budgetRepository.findByUser(user).stream()
                .map(this::toResponse)
                .toList();
    }

    public BudgetResponse setBudget(String phoneNumber, BudgetRequest request) {
        User user = findUser(phoneNumber);

        // Upsert — update if exists, create if not
        Optional<Budget> existing = budgetRepository.findByUserAndCategory(user, request.getCategory());

        Budget budget;
        if (existing.isPresent()) {
            budget = existing.get();
            budget.setLimit(request.getLimit());
            budget.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        } else {
            budget = Budget.builder()
                    .user(user)
                    .category(request.getCategory())
                    .limit(request.getLimit())
                    .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                    .build();
        }

        budgetRepository.save(budget);
        return toResponse(budget);
    }

    @Transactional
    public void removeBudget(String phoneNumber, String category) {
        User user = findUser(phoneNumber);
        budgetRepository.deleteByUserAndCategory(user, category);
    }

    private User findUser(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private BudgetResponse toResponse(Budget b) {
        return new BudgetResponse(b.getId(), b.getCategory(), b.getLimit(), b.getCurrency());
    }
}
