package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.wing_safe_pay.dto.ChangePasswordRequest;
import com.wingsafepay.wing_safe_pay.dto.UpdateProfileRequest;
import com.wingsafepay.wing_safe_pay.dto.UserProfileResponse;
import com.wingsafepay.wing_safe_pay.exception.BadRequestException;
import com.wingsafepay.wing_safe_pay.exception.ConflictException;
import com.wingsafepay.wing_safe_pay.exception.NotFoundException;
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
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));
        long total = transactionRepository.countByUser(user);
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                total,
                user.getProfileImage()
        );
    }

    public UserProfileResponse updateProfile(String phoneNumber, UpdateProfileRequest request) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Update full name if provided
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }

        // Update phone number if provided and different
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()
                && !request.getPhoneNumber().equals(phoneNumber)) {
            // Check if new phone number is already taken
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new ConflictException("Phone number already in use");
            }
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }

        // Update profile image if provided
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }

        userRepository.save(user);

        long total = transactionRepository.countByUser(user);
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                total,
                user.getProfileImage()
        );
    }

    public void changePassword(String phoneNumber, ChangePasswordRequest request) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}