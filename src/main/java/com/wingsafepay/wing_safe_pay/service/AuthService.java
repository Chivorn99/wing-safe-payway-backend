package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.wing_safe_pay.dto.AuthRequest;
import com.wingsafepay.wing_safe_pay.dto.AuthResponse;
import com.wingsafepay.wing_safe_pay.exception.ConflictException;
import com.wingsafepay.wing_safe_pay.exception.BadRequestException;
import com.wingsafepay.wing_safe_pay.exception.UnauthorizedException;
import com.wingsafepay.wing_safe_pay.model.User;
import com.wingsafepay.wing_safe_pay.repository.UserRepository;
import com.wingsafepay.wing_safe_pay.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ConflictException("Phone number already registered");
        }

        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new BadRequestException("Full name is required for registration");
        }

        User user = User.builder()
                .phoneNumber(request.getPhoneNumber())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getPhoneNumber());
        return new AuthResponse(token, user.getFullName(), user.getId());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new UnauthorizedException("Invalid phone number or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid phone number or password");
        }

        String token = jwtUtil.generateToken(user.getPhoneNumber());
        return new AuthResponse(token, user.getFullName(), user.getId());
    }
}