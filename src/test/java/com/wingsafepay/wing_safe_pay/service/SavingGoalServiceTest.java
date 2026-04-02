package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.wing_safe_pay.dto.SavingGoalRequest;
import com.wingsafepay.wing_safe_pay.dto.SavingGoalResponse;
import com.wingsafepay.wing_safe_pay.dto.GoalProgressRequest;
import com.wingsafepay.wing_safe_pay.enums.Role;
import com.wingsafepay.wing_safe_pay.enums.SavingGoalStatus;
import com.wingsafepay.wing_safe_pay.exception.ForbiddenException;
import com.wingsafepay.wing_safe_pay.model.SavingGoal;
import com.wingsafepay.wing_safe_pay.model.User;
import com.wingsafepay.wing_safe_pay.repository.SavingGoalRepository;
import com.wingsafepay.wing_safe_pay.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingGoalServiceTest {

    @Mock private SavingGoalRepository savingGoalRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private SavingGoalService savingGoalService;

    private User testUser() {
        return User.builder().id(1L).phoneNumber("012345678").fullName("Test").password("x").role(Role.USER).build();
    }

    @Test
    void create_success() {
        User user = testUser();
        SavingGoalRequest req = new SavingGoalRequest();
        req.setTitle("Emergency Fund");
        req.setTargetAmount(new BigDecimal("1000"));

        when(userRepository.findByPhoneNumber("012345678")).thenReturn(Optional.of(user));
        when(savingGoalRepository.save(any(SavingGoal.class))).thenAnswer(i -> {
            SavingGoal g = i.getArgument(0);
            g.setId(1L);
            g.setCurrentAmount(BigDecimal.ZERO);
            g.setStatus(SavingGoalStatus.ACTIVE);
            g.setCurrency("USD");
            g.setEmoji("🎯");
            return g;
        });

        SavingGoalResponse resp = savingGoalService.create("012345678", req);

        assertEquals("Emergency Fund", resp.getTitle());
        assertEquals(0, resp.getCurrentAmount().compareTo(BigDecimal.ZERO));
        verify(savingGoalRepository).save(any());
    }

    @Test
    void addProgress_ownershipCheck_throwsForbidden() {
        User owner = testUser();
        User stranger = User.builder().id(99L).phoneNumber("099999999").fullName("Stranger").password("x").role(Role.USER).build();

        SavingGoal goal = SavingGoal.builder()
                .id(1L).user(owner).title("Test").targetAmount(new BigDecimal("100"))
                .currentAmount(BigDecimal.ZERO).status(SavingGoalStatus.ACTIVE).build();

        GoalProgressRequest req = new GoalProgressRequest();
        req.setAmount(new BigDecimal("50"));

        when(userRepository.findByPhoneNumber("099999999")).thenReturn(Optional.of(stranger));
        when(savingGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(ForbiddenException.class,
                () -> savingGoalService.addProgress("099999999", 1L, req));
    }

    @Test
    void addProgress_completesGoal() {
        User user = testUser();
        SavingGoal goal = SavingGoal.builder()
                .id(1L).user(user).title("Trip").targetAmount(new BigDecimal("100"))
                .currentAmount(new BigDecimal("80")).status(SavingGoalStatus.ACTIVE)
                .currency("USD").emoji("✈️").build();

        GoalProgressRequest req = new GoalProgressRequest();
        req.setAmount(new BigDecimal("30"));

        when(userRepository.findByPhoneNumber("012345678")).thenReturn(Optional.of(user));
        when(savingGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(savingGoalRepository.save(any(SavingGoal.class))).thenAnswer(i -> i.getArgument(0));

        SavingGoalResponse resp = savingGoalService.addProgress("012345678", 1L, req);

        assertEquals(SavingGoalStatus.COMPLETED, resp.getStatus());
        assertEquals(0, resp.getCurrentAmount().compareTo(new BigDecimal("110")));
    }
}
