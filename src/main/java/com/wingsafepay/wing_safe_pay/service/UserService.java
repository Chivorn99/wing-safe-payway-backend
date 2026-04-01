package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.wing_safe_pay.dto.ChangePasswordRequest;
import com.wingsafepay.wing_safe_pay.dto.UserProfileResponse;
import com.wingsafepay.wing_safe_pay.model.User;
import com.wingsafepay.wing_safe_pay.repository.TransactionRepository;
import com.wingsafepay.wing_safe_pay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getProfile(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow();
        long total = transactionRepository.countByUser(user);
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                total
        );
    }

    public void changePassword(String phoneNumber, ChangePasswordRequest request) {
        User user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}