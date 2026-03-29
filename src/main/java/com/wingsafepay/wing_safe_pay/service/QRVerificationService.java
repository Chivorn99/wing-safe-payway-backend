package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.wing_safe_pay.dto.QRCheckRequest;
import com.wingsafepay.wing_safe_pay.dto.QRCheckResponse;
import com.wingsafepay.wing_safe_pay.enums.PaymentContext;
import com.wingsafepay.wing_safe_pay.enums.RiskLevel;
import com.wingsafepay.wing_safe_pay.enums.TransactionCategory;
import com.wingsafepay.wing_safe_pay.model.Merchant;
import com.wingsafepay.wing_safe_pay.repository.MerchantRepository;
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

        Optional<Merchant> merchantOpt = merchantRepository.findByMerchantId(request.getMerchantId());
        Merchant merchant = merchantOpt.orElse(null);

        if (merchant != null) {
            passedChecks.add("Merchant code recognized");
            if (merchant.isVerified()) {
                passedChecks.add("Merchant is Wing-verified");
            } else {
                warnings.add("Merchant exists but is not verified");
            }
        } else {
            warnings.add("Merchant code not found");
        }

        if (request.getDisplayedName() != null && !request.getDisplayedName().isBlank()) {
            passedChecks.add("Recipient name present");

            if (merchant != null && !merchant.getMerchantName().equalsIgnoreCase(request.getDisplayedName().trim())) {
                warnings.add("Displayed name does not match merchant record");
            } else if (merchant != null) {
                passedChecks.add("Merchant name recognized");
            }
        } else {
            warnings.add("Recipient name missing");
        }

        if (request.getBankName() != null && !request.getBankName().isBlank()) {
            passedChecks.add("Bank name present");

            if (merchant != null && merchant.getBankName() != null
                    && !merchant.getBankName().equalsIgnoreCase(request.getBankName().trim())) {
                warnings.add("Bank name does not match merchant record");
            }
        } else {
            warnings.add("Bank name missing");
        }

        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                warnings.add("Amount must be greater than 0");
            } else if (request.getAmount().compareTo(new BigDecimal("1000")) > 0) {
                warnings.add("High transaction amount");
            } else {
                passedChecks.add("Amount within normal range");
            }
        } else {
            warnings.add("Amount missing");
        }

        if (request.getQrType() != null && !request.getQrType().isBlank()) {
            passedChecks.add("Standard " + request.getQrType() + " QR");
        } else {
            warnings.add("QR type missing");
        }

        PaymentContext context = request.getPaymentContext() != null
                ? request.getPaymentContext()
                : PaymentContext.MERCHANT;

        TransactionCategory category = inferCategory(context, merchant, request.getDisplayedName());
        RiskLevel riskLevel = resolveRiskLevel(warnings, merchant);

        String message = switch (riskLevel) {
            case SAFE -> "This looks like a valid payment request.";
            case WARNING -> "Proceed with caution. Some details need attention.";
            case HIGH_RISK -> "This payment request looks suspicious.";
        };

        return QRCheckResponse.builder()
                .recipientName(request.getDisplayedName())
                .bankName(request.getBankName())
                .amount(request.getAmount())
                .qrType(request.getQrType())
                .paymentContext(context)
                .riskLevel(riskLevel)
                .category(category)
                .warnings(warnings)
                .passedChecks(passedChecks)
                .message(message)
                .build();
    }

    private RiskLevel resolveRiskLevel(List<String> warnings, Merchant merchant) {
        if (warnings.stream().anyMatch(w ->
                w.contains("not found") ||
                        w.contains("does not match") ||
                        w.contains("missing"))) {
            return RiskLevel.HIGH_RISK;
        }

        if (!warnings.isEmpty() || (merchant != null && !merchant.isVerified())) {
            return RiskLevel.WARNING;
        }

        return RiskLevel.SAFE;
    }

    private TransactionCategory inferCategory(PaymentContext context, Merchant merchant, String displayedName) {
        if (context == PaymentContext.WINGSHOP) return TransactionCategory.SHOPPING;
        if (context == PaymentContext.BILLPAY) return TransactionCategory.UTILITIES;
        if (context == PaymentContext.P2P) return TransactionCategory.TRANSFER;

        if (merchant != null && merchant.getCategory() != null) {
            try {
                return TransactionCategory.valueOf(merchant.getCategory().toUpperCase());
            } catch (Exception ignored) {}
        }

        if (displayedName == null) return TransactionCategory.OTHER;

        String lower = displayedName.toLowerCase();
        if (lower.contains("coffee") || lower.contains("cafe") || lower.contains("restaurant")) {
            return TransactionCategory.FOOD;
        }
        if (lower.contains("shop") || lower.contains("store")) {
            return TransactionCategory.SHOPPING;
        }
        return TransactionCategory.OTHER;
    }
}