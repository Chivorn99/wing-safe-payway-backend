package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.wing_safe_pay.dto.AuthRequest;
import com.wingsafepay.wing_safe_pay.dto.AuthResponse;
import com.wingsafepay.wing_safe_pay.enums.Role;
import com.wingsafepay.wing_safe_pay.exception.ConflictException;
import com.wingsafepay.wing_safe_pay.exception.UnauthorizedException;
import com.wingsafepay.wing_safe_pay.model.User;
import com.wingsafepay.wing_safe_pay.repository.UserRepository;
import com.wingsafepay.wing_safe_pay.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @InjectMocks private AuthService authService;

    @Test
    void register_success() {
        AuthRequest req = new AuthRequest();
        req.setPhoneNumber("012345678");
        req.setPassword("secret");
        req.setFullName("Test User");

        when(userRepository.existsByPhoneNumber("012345678")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            u.setRole(Role.USER);
            return u;
        });
        when(jwtUtil.generateToken("012345678", "USER")).thenReturn("jwt-token");

        AuthResponse resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("jwt-token", resp.getToken());
        assertEquals("Test User", resp.getFullName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicatePhone_throwsConflict() {
        AuthRequest req = new AuthRequest();
        req.setPhoneNumber("012345678");
        req.setPassword("secret");
        req.setFullName("Test");

        when(userRepository.existsByPhoneNumber("012345678")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        AuthRequest req = new AuthRequest();
        req.setPhoneNumber("012345678");
        req.setPassword("secret");

        User user = User.builder()
                .id(1L).phoneNumber("012345678")
                .fullName("Test").password("hashed").role(Role.USER).build();

        when(userRepository.findByPhoneNumber("012345678")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("012345678", "USER")).thenReturn("jwt-token");

        AuthResponse resp = authService.login(req);

        assertEquals("jwt-token", resp.getToken());
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        AuthRequest req = new AuthRequest();
        req.setPhoneNumber("012345678");
        req.setPassword("wrong");

        User user = User.builder()
                .id(1L).phoneNumber("012345678")
                .fullName("Test").password("hashed").role(Role.USER).build();

        when(userRepository.findByPhoneNumber("012345678")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }

    @Test
    void login_phoneNotFound_throwsUnauthorized() {
        AuthRequest req = new AuthRequest();
        req.setPhoneNumber("999999999");
        req.setPassword("secret");

        when(userRepository.findByPhoneNumber("999999999")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }
}
