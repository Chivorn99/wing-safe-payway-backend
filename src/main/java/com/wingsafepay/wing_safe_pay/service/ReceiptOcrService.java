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
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ReceiptOcrService {

    @Value("${app.tesseract.path}")
    private String tessDataPath;

    public ReceiptScanResponse scan(MultipartFile file) {
        String rawText = extractText(file);
        return parseReceipt(rawText);
    }

    private String extractText(MultipartFile file) {
        try {
            BufferedImage original = ImageIO.read(file.getInputStream());
            if (original == null) {
                return "";
            }
            BufferedImage preprocessed = preprocess(original);

            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(6);
            tesseract.setOcrEngineMode(1);

            return tesseract.doOCR(preprocessed);
        } catch (TesseractException | IOException e) {
            return "";
        }
    }

    private BufferedImage preprocess(BufferedImage source) {
        int maxWidth = 1200;
        int w = source.getWidth();
        int h = source.getHeight();

        // Only resize if image is too large (downscale) or too small (upscale)
        if (w > maxWidth) {
            double scale = (double) maxWidth / w;
            w = maxWidth;
            h = (int) (h * scale);
        } else if (w < 600) {
            double scale = 600.0 / w;
            w = 600;
            h = (int) (h * scale);
        } else {
            // Image is already a good size — just convert to grayscale
            BufferedImage gray = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = gray.createGraphics();
            g.drawImage(source, 0, 0, null);
            g.dispose();
            return gray;
        }

        // Resize + grayscale in one pass
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, w, h, null);
        g.dispose();

        return result;
    }

    private ReceiptScanResponse parseReceipt(String text) {
        String lower = text.toLowerCase();
        List<String> lines = List.of(text.split("\\n"));

        BigDecimal amount = extractAmount(text);
        String recipientName = extractMerchantName(lines);
        String bankName = extractBankName(lower);
        String currency = extractCurrency(text);
        TransactionCategory category = guessCategory(lower);

        return ReceiptScanResponse.builder()
                .recipientName(recipientName)
                .bankName(bankName)
                .amount(amount)
                .currency(currency)
                .category(category)
                .riskLevel(RiskLevel.SAFE)
                .paymentContext(PaymentContext.MERCHANT)
                .status(TransactionStatus.PAID)
                .note("Auto-filled from receipt - please review")
                .rawText(text.length() > 600 ? text.substring(0, 600) : text)
                .build();
    }

    private BigDecimal extractAmount(String text) {
        Pattern labeled = Pattern.compile(
                "(?:total|grand total|amount due|subtotal|charged)[^\\d$]*(\\$?\\d{1,6}[.,]\\d{2})",
                Pattern.CASE_INSENSITIVE
        );
        Matcher m = labeled.matcher(text);
        if (m.find()) {
            return parseMoney(m.group(1));
        }

        Pattern any = Pattern.compile("\\$?\\s*(\\d{1,6}[.,]\\d{2})");
        Matcher anyM = any.matcher(text);
        BigDecimal largest = BigDecimal.ZERO;
        while (anyM.find()) {
            BigDecimal v = parseMoney(anyM.group(1));
            if (v.compareTo(largest) > 0) {
                largest = v;
            }
        }
        return largest;
    }

    private BigDecimal parseMoney(String raw) {
        try {
            return new BigDecimal(raw.replace("$", "").replace(",", ".").trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String extractMerchantName(List<String> lines) {
        List<String> candidates = new ArrayList<>();
        for (String line : lines) {
            String t = line.trim();
            if (t.length() < 3 || t.length() > 50) {
                continue;
            }
            if (t.matches(".*\\d{2}/\\d{2}.*")) {
                continue;
            }
            if (t.matches(".*\\$.*")) {
                continue;
            }
            if (t.matches("[0-9\\s.,]+")) {
                continue;
            }
            if (t.matches("[A-Za-z0-9 &'.,\\-]+")) {
                candidates.add(t);
            }
        }
        return candidates.isEmpty() ? "Unknown Merchant" : candidates.get(0);
    }

    private String extractBankName(String lower) {
        if (lower.contains("aba")) return "ABA Bank";
        if (lower.contains("acleda")) return "ACLEDA Bank";
        if (lower.contains("wing")) return "Wing Bank";
        if (lower.contains("visa")) return "Visa";
        if (lower.contains("mastercard")) return "Mastercard";
        if (lower.contains("paypal")) return "PayPal";
        if (lower.contains("stripe")) return "Stripe";
        return "Unknown";
    }

    private String extractCurrency(String text) {
        String upper = text.toUpperCase();
        if (text.contains("$") || upper.contains("USD")) return "USD";
        if (text.contains("\\u20AC") || upper.contains("EUR")) return "EUR";
        if (upper.contains("KHR") || text.contains("\\u17DB")) return "KHR";
        if (upper.contains("THB") || text.contains("\\u0E3F")) return "THB";
        return "USD";
    }

    private TransactionCategory guessCategory(String lower) {
        if (lower.contains("restaurant") || lower.contains("coffee")
                || lower.contains("cafe") || lower.contains("food")
                || lower.contains("burger") || lower.contains("pizza")
                || lower.contains("noodle") || lower.contains("bbq")) {
            return TransactionCategory.FOOD;
        }
        if (lower.contains("grab") || lower.contains("taxi")
                || lower.contains("parking") || lower.contains("fuel")
                || lower.contains("transport") || lower.contains("bus")
                || lower.contains("train") || lower.contains("flight")) {
            return TransactionCategory.TRANSPORT;
        }
        if (lower.contains("pharmacy") || lower.contains("hospital")
                || lower.contains("clinic") || lower.contains("dental")
                || lower.contains("health") || lower.contains("medical")) {
            return TransactionCategory.HEALTH;
        }
        if (lower.contains("electricity") || lower.contains("water")
                || lower.contains("internet") || lower.contains("bill")
                || lower.contains("utility") || lower.contains("telco")) {
            return TransactionCategory.UTILITIES;
        }
        if (lower.contains("mart") || lower.contains("supermarket")
                || lower.contains("market") || lower.contains("grocery")
                || lower.contains("fresh") || lower.contains("shop")) {
            return TransactionCategory.SHOPPING;
        }
        if (lower.contains("school") || lower.contains("university")
                || lower.contains("tuition") || lower.contains("education")
                || lower.contains("course") || lower.contains("book")) {
            return TransactionCategory.EDUCATION;
        }
        if (lower.contains("cinema") || lower.contains("netflix")
                || lower.contains("spotify") || lower.contains("game")
                || lower.contains("entertainment")) {
            return TransactionCategory.ENTERTAINMENT;
        }
        return TransactionCategory.OTHER;
    }
}