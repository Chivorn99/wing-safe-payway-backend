package com.wingsafepay.wing_safe_pay.repository;

import com.wingsafepay.wing_safe_pay.enums.TransactionStatus;
import com.wingsafepay.wing_safe_pay.model.Transaction;
import com.wingsafepay.wing_safe_pay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
    long countByUserAndStatus(User user, TransactionStatus status);
    long countByUser(User user);
}