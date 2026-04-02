package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.wing_safe_pay.dto.GoalProgressRequest;
import com.wingsafepay.wing_safe_pay.dto.SavingGoalRequest;
import com.wingsafepay.wing_safe_pay.dto.SavingGoalResponse;
import com.wingsafepay.wing_safe_pay.enums.SavingGoalStatus;
import com.wingsafepay.wing_safe_pay.exception.ForbiddenException;
import com.wingsafepay.wing_safe_pay.exception.NotFoundException;
import com.wingsafepay.wing_safe_pay.model.SavingGoal;
import com.wingsafepay.wing_safe_pay.model.User;
import com.wingsafepay.wing_safe_pay.repository.SavingGoalRepository;
import com.wingsafepay.wing_safe_pay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingGoalService {

    private final SavingGoalRepository savingGoalRepository;
    private final UserRepository userRepository;

    public SavingGoalResponse create(String phoneNumber, SavingGoalRequest request) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));

        SavingGoal goal = SavingGoal.builder()
                .user(user)
                .title(request.getTitle())
                .targetAmount(request.getTargetAmount())
                .deadline(request.getDeadline())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .emoji(request.getEmoji() != null ? request.getEmoji() : "🎯")
                .currentAmount(BigDecimal.ZERO)
                .status(SavingGoalStatus.ACTIVE)
                .build();

        return toResponse(savingGoalRepository.save(goal));
    }

    public List<SavingGoalResponse> getUserGoals(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return savingGoalRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SavingGoalResponse addProgress(String phoneNumber, Long goalId, GoalProgressRequest request) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));

        SavingGoal goal = savingGoalRepository.findById(goalId)
                .orElseThrow(() -> new NotFoundException("Goal not found"));

        // Ownership check
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not own this goal");
        }

        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingGoalStatus.COMPLETED);
        }

        return toResponse(savingGoalRepository.save(goal));
    }

    public void deleteGoal(String phoneNumber, Long goalId) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found"));

        SavingGoal goal = savingGoalRepository.findById(goalId)
                .orElseThrow(() -> new NotFoundException("Goal not found"));

        // Ownership check
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not own this goal");
        }

        savingGoalRepository.delete(goal);
    }

    private SavingGoalResponse toResponse(SavingGoal goal) {
        BigDecimal percent = BigDecimal.ZERO;

        if (goal.getTargetAmount() != null && goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percent = goal.getCurrentAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP);
        }

        return SavingGoalResponse.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount())
                .progressPercent(percent)
                .deadline(goal.getDeadline())
                .status(goal.getStatus())
                .currency(goal.getCurrency())
                .emoji(goal.getEmoji())
                .createdAt(goal.getCreatedAt())
                .build();
    }
}