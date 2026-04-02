package com.wingsafepay.wing_safe_pay.controller;

import com.wingsafepay.wing_safe_pay.dto.ChangePasswordRequest;
import com.wingsafepay.wing_safe_pay.dto.UserProfileResponse;
import com.wingsafepay.wing_safe_pay.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication auth) {
        return ResponseEntity.ok(userService.getProfile(auth.getName()));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            Authentication auth,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(auth.getName(), request);
        return ResponseEntity.ok().build();
    }
}