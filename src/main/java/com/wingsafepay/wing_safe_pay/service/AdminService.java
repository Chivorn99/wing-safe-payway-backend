package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.wing_safe_pay.repository.SavingGoalRepository;
import com.wingsafepay.wing_safe_pay.repository.TransactionRepository;
import com.wingsafepay.wing_safe_pay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SavingGoalRepository savingGoalRepository;

    /**
     * Returns a list of all users (admin-only).
     * Projections: id, phoneNumber, fullName, role, createdAt
     */
    public java.util.List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", user.getId());
                    map.put("phoneNumber", user.getPhoneNumber());
                    map.put("fullName", user.getFullName());
                    map.put("role", user.getRole().name());
                    map.put("createdAt", user.getCreatedAt());
                    return map;
                })
                .toList();
    }

    /**
     * Returns platform-wide statistics (admin-only dashboard).
     */
    public Map<String, Object> getPlatformStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalTransactions", transactionRepository.count());
        stats.put("totalGoals", savingGoalRepository.count());
        return stats;
    }
}
