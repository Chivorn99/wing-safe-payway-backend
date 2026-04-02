package com.wingsafepay.wing_safe_pay.repository;

import com.wingsafepay.wing_safe_pay.enums.TransactionCategory;
import com.wingsafepay.wing_safe_pay.enums.TransactionStatus;
import com.wingsafepay.wing_safe_pay.model.Transaction;
import com.wingsafepay.wing_safe_pay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
    long countByUserAndStatus(User user, TransactionStatus status);
    long countByUser(User user);

    // Explicit JOIN — filter by category for a specific user
    @Query("SELECT t FROM Transaction t JOIN t.user u WHERE u.phoneNumber = :phone AND t.category = :category ORDER BY t.createdAt DESC")
    List<Transaction> findByUserPhoneAndCategory(@Param("phone") String phone, @Param("category") TransactionCategory category);

    // Explicit JOIN — keyword search across recipient name and note
    @Query("SELECT t FROM Transaction t JOIN t.user u WHERE u.phoneNumber = :phone AND (LOWER(t.recipientName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.note) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY t.createdAt DESC")
    List<Transaction> searchByKeyword(@Param("phone") String phone, @Param("keyword") String keyword);
}