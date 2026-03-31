package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.wing_safe_pay.dto.ReceiptScanResponse;
import com.wingsafepay.wing_safe_pay.enums.TransactionCategory;
import com.wingsafepay.wing_safe_pay.enums.RiskLevel;
import com.wingsafepay.wing_safe_pay.enums.PaymentContext;
import com.wingsafepay.wing_safe_pay.enums.TransactionStatus;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReceiptOcrService {

    @Value("${tesseract.data-path}")
    private String tessDataPath;

    public ReceiptScanResponse scan(MultipartFile image) {
        try {
            BufferedImage bufferedImage = ImageIO.read(image.getInputStream());

            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("eng");

            String text = tesseract.doOCR(bufferedImage);

            return parse(text);
        } catch (IOException | TesseractException e) {
            return fallbackResponse();
        }
    }

    private ReceiptScanResponse parse(String text) {
        String lowerText = text.toLowerCase();

        BigDecimal amount = extractAmount(text);
        String recipientName = extractMerchant(text);
        TransactionCategory category = guessCategory(lowerText);

        return ReceiptScanResponse.builder()
                .recipientName(recipientName)
                .bankName("Unknown")
                .amount(amount)
                .currency("USD")
                .category(category)
                .riskLevel(RiskLevel.WARNING)
                .paymentContext(PaymentContext.MERCHANT)
                .status(TransactionStatus.PAID)
                .note("Parsed from receipt. Please review and correct.")
                .rawText(text)
                .build();
    }

    private BigDecimal extractAmount(String text) {
        Pattern pattern = Pattern.compile(
                "(?:total|amount|due|pay|grand total)[^\\d]*(\\d+[\\.,]\\d{2})",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String raw = matcher.group(1).replace(",", ".");
            try {
                return new BigDecimal(raw);
            } catch (NumberFormatException ignored) {}
        }

        // fallback: find last price-like number
        Pattern fallback = Pattern.compile("(\\d{1,6}[\\.,]\\d{2})");
        Matcher fb = fallback.matcher(text);
        BigDecimal last = null;
        while (fb.find()) {
            try {
                last = new BigDecimal(fb.group(1).replace(",", "."));
            } catch (NumberFormatException ignored) {}
        }
        return last != null ? last : BigDecimal.ZERO;
    }

    private String extractMerchant(String text) {
        String[] lines = text.split("\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() > 3 && trimmed.length() < 50
                    && trimmed.matches(".*[a-zA-Z].*")
                    && !trimmed.matches(".*\\d{3,}.*")) {
                return trimmed;
            }
        }
        return "Unknown Merchant";
    }

    private TransactionCategory guessCategory(String text) {
        if (text.contains("coffee") || text.contains("food") || text.contains("restaurant")
                || text.contains("cafe") || text.contains("pizza") || text.contains("burger"))
            return TransactionCategory.FOOD;
        if (text.contains("shop") || text.contains("store") || text.contains("mart"))
            return TransactionCategory.SHOPPING;
        if (text.contains("transport") || text.contains("grab") || text.contains("taxi")
                || text.contains("fuel") || text.contains("petrol"))
            return TransactionCategory.TRANSPORT;
        if (text.contains("electric") || text.contains("water") || text.contains("internet")
                || text.contains("bill"))
            return TransactionCategory.UTILITIES;
        if (text.contains("hospital") || text.contains("pharmacy") || text.contains("clinic"))
            return TransactionCategory.HEALTH;
        return TransactionCategory.OTHER;
    }

    private ReceiptScanResponse fallbackResponse() {
        return ReceiptScanResponse.builder()
                .recipientName("")
                .bankName("")
                .amount(BigDecimal.ZERO)
                .currency("USD")
                .category(TransactionCategory.OTHER)
                .riskLevel(RiskLevel.WARNING)
                .paymentContext(PaymentContext.MERCHANT)
                .status(TransactionStatus.PAID)
                .note("OCR failed. Please fill in manually.")
                .rawText("")
                .build();
    }
}