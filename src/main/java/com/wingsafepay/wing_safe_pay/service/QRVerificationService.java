package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.dto.QRCheckRequest;
import com.wingsafepay.dto.QRCheckResponse;
import com.wingsafepay.enums.RiskLevel;
import com.wingsafepay.enums.TransactionCategory;
import com.wingsafepay.model.Merchant;
import com.wingsafepay.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QRVerificationService {

    private final MerchantRepository merchantRepository;

    public QRCheckResponse verify(QRCheckRequest request) {
        List<String> warnings = new ArrayList<>();
        List<String> passedChecks = new ArrayList<>();
        RiskLevel riskLevel = RiskLevel.SAFE;
        TransactionCategory category = TransactionCategory.OTHER;

        // 1. Check if merchant exists in verified DB
        Optional<Merchant> merchant = merchantRepository.findByMerchantId(request.getMerchantId());

        if (merchant.isPresent()) {
            passedChecks.add("Merchant is Wing-verified ✓");
            passedChecks.add("Merchant name recognized ✓");
            category = mapCategory(merchant.get().getCategory());
        } else {
            warnings.add("Merchant not found in Wing verified list");
            riskLevel = RiskLevel.WARNING;
        }

        // 2. Check QR type: merchant vs personal
        if ("PERSONAL".equalsIgnoreCase(request.getQrType())) {
            warnings.add("This is a personal QR, not a merchant QR");
            riskLevel = RiskLevel.WARNING;
        } else {
            passedChecks.add("Standard merchant QR ✓");
        }

        // 3. Check amount — flag if unusually high
        if (request.getAmount() != null && request.getAmount().compareTo(new BigDecimal("500")) > 0) {
            warnings.add("Unusually high amount for a single payment");
            riskLevel = escalate(riskLevel, RiskLevel.WARNING);
        } else if (request.getAmount() != null) {
            passedChecks.add("Amount within normal range ✓");
        }

        // 4. Check if amount is missing (bad QR)
        if (request.getAmount() == null) {
            warnings.add("No amount specified — confirm with merchant before paying");
        }

        // 5. Name mismatch check
        if (merchant.isPresent() &&
                !merchant.get().getMerchantName().equalsIgnoreCase(request.getDisplayedName())) {
            warnings.add("Displayed name does not match our merchant record");
            riskLevel = escalate(riskLevel, RiskLevel.HIGH_RISK);
        }

        // 6. Missing bank field
        if (request.getBankName() == null || request.getBankName().isBlank()) {
            warnings.add("Bank name is missing from QR");
            riskLevel = escalate(riskLevel, RiskLevel.WARNING);
        } else {
            passedChecks.add("Bank name present ✓");
        }

        String message = switch (riskLevel) {
            case SAFE -> "This looks like a valid merchant payment.";
            case WARNING -> "Please double-check the recipient before paying.";
            case HIGH_RISK -> "This QR looks suspicious. We recommend not proceeding.";
        };

        return QRCheckResponse.builder()
                .recipientName(request.getDisplayedName())
                .bankName(request.getBankName())
                .amount(request.getAmount())
                .qrType(request.getQrType())
                .riskLevel(riskLevel)
                .category(category)
                .warnings(warnings)
                .passedChecks(passedChecks)
                .message(message)
                .build();
    }

    private RiskLevel escalate(RiskLevel current, RiskLevel next) {
        return current.ordinal() >= next.ordinal() ? current : next;
    }

    private TransactionCategory mapCategory(String cat) {
        if (cat == null) return TransactionCategory.OTHER;
        return switch (cat.toLowerCase()) {
            case "food", "restaurant", "beverage" -> TransactionCategory.FOOD;
            case "retail", "shopping", "market" -> TransactionCategory.SHOPPING;
            case "transport", "taxi", "grab" -> TransactionCategory.TRANSPORT;
            case "health", "pharmacy", "clinic" -> TransactionCategory.HEALTH;
            default -> TransactionCategory.OTHER;
        };
    }
}
