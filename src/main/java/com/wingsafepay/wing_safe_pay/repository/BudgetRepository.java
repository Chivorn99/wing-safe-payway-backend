package com.wingsafepay.wing_safe_pay.repository;

import com.wingsafepay.wing_safe_pay.model.Budget;
import com.wingsafepay.wing_safe_pay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUser(User user);

    Optional<Budget> findByUserAndCategory(User user, String category);

    void deleteByUserAndCategory(User user, String category);
}
