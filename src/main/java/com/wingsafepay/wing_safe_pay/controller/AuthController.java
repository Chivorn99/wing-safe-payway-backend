package com.wingsafepay.wing_safe_pay.controller;

import com.wingsafepay.wing_safe_pay.dto.AuthRequest;
import com.wingsafepay.wing_safe_pay.dto.AuthResponse;
import com.wingsafepay.wing_safe_pay.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestParam String fullName,
            @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.register(request, fullName));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}