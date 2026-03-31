package com.wingsafepay.wing_safe_pay.service;

import com.wingsafepay.wing_safe_pay.dto.ReceiptScanResponse;
import com.wingsafepay.wing_safe_pay.enums.PaymentContext;
import com.wingsafepay.wing_safe_pay.enums.RiskLevel;
import com.wingsafepay.wing_safe_pay.enums.TransactionCategory;
import com.wingsafepay.wing_safe_pay.enums.TransactionStatus;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
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

    public ReceiptScanResponse scan(MultipartFile file) {
        String raw = extractText(file);
        return parseReceipt(raw);
    }

    private String extractText(MultipartFile file) {
        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
            tesseract.setLanguage("eng");
            BufferedImage image = ImageIO.read(file.getInputStream());
            return tesseract.doOCR(image);
        } catch (TesseractException | IOException e) {
            return "";
        }
    }

    private ReceiptScanResponse parseReceipt(String text) {
        String lower = text.toLowerCase();

        return ReceiptScanResponse.builder()
                .recipientName(extractSeller(text))
                .bankName(extractBank(text))
                .amount(extractAmount(text))
                .currency(extractCurrency(text))
                .category(guessCategory(lower))
                .riskLevel(RiskLevel.SAFE)
                .paymentContext(PaymentContext.MERCHANT)
                .status(TransactionStatus.PAID)
                .note("Parsed from receipt - please review and correct")
                .rawText(text.length() > 600 ? text.substring(0, 600) : text)
                .build();
    }

    private BigDecimal extractAmount(String text) {
        // Try labeled amount fields first: "original amount", "total", "amount"
        String[] labels = {
                "original amount[:\\s]+([\\d,\\.]+)",
                "total[:\\s]+([\\d,\\.]+)",
                "amount[:\\s]+([\\d,\\.]+)",
                "grand total[:\\s]+([\\d,\\.]+)"
        };

        for (String label : labels) {
            Pattern p = Pattern.compile(label, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                BigDecimal parsed = parseAmountString(m.group(1));
                if (parsed.compareTo(BigDecimal.ZERO) > 0) return parsed;
            }
        }

        // Fallback: find largest number in text
        Pattern anyNum = Pattern.compile("[\\d]{1,3}(?:[,\\s][\\d]{3})*(?:[.][\\d]{1,2})?");
        Matcher m = anyNum.matcher(text);
        BigDecimal largest = BigDecimal.ZERO;

        while (m.find()) {
            BigDecimal val = parseAmountString(m.group());
            if (val.compareTo(largest) > 0) largest = val;
        }

        return largest;
    }

    /**
     * Handles: "12,631.50" → 12631.50
     *           "12.631,50" → 12631.50 (European)
     *           "12631.50"  → 12631.50
     *           "12,631"    → 12631
     */
    private BigDecimal parseAmountString(String raw) {
        String s = raw.trim().replaceAll("\\s", "");

        try {
            // Format like 12,631.50 (comma=thousands, period=decimal)
            if (s.matches("\\d{1,3}(,\\d{3})+(\\.[\\d]{1,2})?")) {
                s = s.replace(",", "");
                return new BigDecimal(s);
            }

            // Format like 12.631,50 (period=thousands, comma=decimal)
            if (s.matches("\\d{1,3}(\\.\\d{3})+(,[\\d]{1,2})?")) {
                s = s.replace(".", "").replace(",", ".");
                return new BigDecimal(s);
            }

            // Plain number like 12631.50 or 12631
            s = s.replace(",", "");
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String extractSeller(String text) {
        // Try "Seller:" label first — most reliable
        Pattern sellerPattern = Pattern.compile(
                "(?:seller|merchant|store|shop)[:\\s]+([^\\n]+)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher m = sellerPattern.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }

        // Try first clean-looking line of text (alphanumeric, no noise)
        String[] lines = text.split("\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() >= 3
                    && trimmed.length() <= 60
                    && trimmed.matches("[A-Z0-9][A-Za-z0-9 &'.,\\-]+")) {
                return trimmed;
            }
        }

        return "Unknown Merchant";
    }

    private String extractBank(String text) {
        String upper = text.toUpperCase();

        if (upper.contains("ABA")) return "ABA Bank";
        if (upper.contains("ACLEDA")) return "ACLEDA Bank";
        if (upper.contains("WING")) return "Wing Bank";
        if (upper.contains("CANADIA")) return "Canadia Bank";
        if (upper.contains("CAMBODIA POST")) return "Cambodia Post Bank";
        if (upper.contains("MAYBANK")) return "Maybank";
        if (upper.contains("ANZ")) return "ANZ Royal Bank";
        if (upper.contains("BRED")) return "BRED Bank";
        if (upper.contains("PRINCE")) return "Prince Bank";
        if (upper.contains("AMRET")) return "Amret";
        if (upper.contains("PRASAC")) return "PRASAC";

        return "Unknown";
    }

    private String extractCurrency(String text) {
        String upper = text.toUpperCase();

        if (upper.contains("KHR")) return "KHR";
        if (upper.contains("USD")) return "USD";
        if (upper.contains("THB")) return "THB";
        if (upper.contains("VND")) return "VND";
        if (upper.contains("EUR")) return "EUR";
        if (upper.contains("SGD")) return "SGD";

        return "USD";
    }

    private TransactionCategory guessCategory(String lower) {
        if (lower.contains("restaurant") || lower.contains("coffee")
                || lower.contains("cafe") || lower.contains("food")
                || lower.contains("burger") || lower.contains("pizza")
                || lower.contains("noodle") || lower.contains("drink")) {
            return TransactionCategory.FOOD;
        }
        if (lower.contains("grab") || lower.contains("taxi")
                || lower.contains("parking") || lower.contains("fuel")
                || lower.contains("transport") || lower.contains("bus")) {
            return TransactionCategory.TRANSPORT;
        }
        if (lower.contains("pharmacy") || lower.contains("hospital")
                || lower.contains("clinic") || lower.contains("health")) {
            return TransactionCategory.HEALTH;
        }
        if (lower.contains("electricity") || lower.contains("water")
                || lower.contains("internet") || lower.contains("bill")
                || lower.contains("utilities")) {
            return TransactionCategory.UTILITIES;
        }
        if (lower.contains("mart") || lower.contains("market")
                || lower.contains("grocery") || lower.contains("supermarket")
                || lower.contains("shop") || lower.contains("store")) {
            return TransactionCategory.SHOPPING;
        }
        return TransactionCategory.OTHER;
    }
}