package com.wingsafepay.wing_safe_pay.repository;

import com.wingsafepay.wing_safe_pay.model.SavingGoal;
import com.wingsafepay.wing_safe_pay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavingGoalRepository extends JpaRepository<SavingGoal, Long> {
    List<SavingGoal> findByUserOrderByCreatedAtDesc(User user);
}