package com.wingsafepay.wing_safe_pay.config;

import com.wingsafepay.wing_safe_pay.enums.*;
import com.wingsafepay.wing_safe_pay.model.SavingGoal;
import com.wingsafepay.wing_safe_pay.model.Transaction;
import com.wingsafepay.wing_safe_pay.model.User;
import com.wingsafepay.wing_safe_pay.repository.SavingGoalRepository;
import com.wingsafepay.wing_safe_pay.repository.TransactionRepository;
import com.wingsafepay.wing_safe_pay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SavingGoalRepository savingGoalRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEMO_PHONE = "0961234567";
    private static final String ADMIN_PHONE = "0960000000";

    @Override
    public void run(String... args) {
        if (userRepository.existsByPhoneNumber(DEMO_PHONE)) {
            log.info("⏭️  Demo user already exists, skipping seed.");
            return;
        }

        log.info("🌱 Seeding demo data...");

        // ── 1. Create demo user (role: USER) ─────────────────────────────────
        User user = User.builder()
                .phoneNumber(DEMO_PHONE)
                .fullName("Chivorn Sok")
                .password(passwordEncoder.encode("123456"))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);

        // ── 1b. Create admin user (role: ADMIN) ──────────────────────────────
        if (!userRepository.existsByPhoneNumber(ADMIN_PHONE)) {
            User admin = User.builder()
                    .phoneNumber(ADMIN_PHONE)
                    .fullName("Admin WingView")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("   ✅ Admin user seeded (phone: {}, password: admin123)", ADMIN_PHONE);
        }

        // ── 2. Seed transactions (last 3 months) ────────────────────────────
        LocalDateTime now = LocalDateTime.now();

        List<TxSeed> seeds = List.of(
                // ---- This month ----
                new TxSeed("Brown Coffee", "ABA Bank", 4.50, "USD", TransactionCategory.FOOD, "Morning latte ☕", 1),
                new TxSeed("Nham Nham Restaurant", "Wing Bank", 8.75, "USD", TransactionCategory.FOOD, "Lunch with team", 2),
                new TxSeed("Street Noodles", "Wing Bank", 1.50, "USD", TransactionCategory.FOOD, "Quick breakfast", 3),
                new TxSeed("Lucky Supermarket", "ABA Bank", 32.40, "USD", TransactionCategory.SHOPPING, "Weekly groceries 🛒", 2),
                new TxSeed("Grab Cambodia", "Wing Bank", 3.25, "USD", TransactionCategory.TRANSPORT, "Ride to BKK1", 1),
                new TxSeed("PassApp Taxi", "Wing Bank", 2.50, "USD", TransactionCategory.TRANSPORT, "Airport pickup", 4),
                new TxSeed("EDC Electricity", "ABA Bank", 28.00, "USD", TransactionCategory.UTILITIES, "March electricity", 5),
                new TxSeed("PPWSA Water", "ABA Bank", 8.50, "USD", TransactionCategory.UTILITIES, "Water bill", 5),
                new TxSeed("Smart Axiata", "Wing Bank", 10.00, "USD", TransactionCategory.UTILITIES, "Phone plan top-up", 3),
                new TxSeed("Phnom Penh Pharmacy", "ABA Bank", 12.00, "USD", TransactionCategory.HEALTH, "Cold medicine", 6),
                new TxSeed("Kim Sok", "Wing Bank", 50.00, "USD", TransactionCategory.TRANSFER, "Repay lunch money", 2),
                new TxSeed("Netflix", "ABA Bank", 6.99, "USD", TransactionCategory.ENTERTAINMENT, "Monthly subscription", 1),
                new TxSeed("Spotify", "ABA Bank", 4.99, "USD", TransactionCategory.ENTERTAINMENT, "Music premium", 1),
                new TxSeed("Amazon Kindle", "ABA Bank", 9.99, "USD", TransactionCategory.EDUCATION, "eBook purchase 📚", 4),
                new TxSeed("AEON Mall", "ABA Bank", 45.00, "USD", TransactionCategory.SHOPPING, "New shirt & shoes", 7),

                // ---- Last month (offset +30 days ago) ----
                new TxSeed("Brown Coffee", "ABA Bank", 4.50, "USD", TransactionCategory.FOOD, "Afternoon iced latte", 32),
                new TxSeed("Sushi Bar Tomo", "ABA Bank", 18.50, "USD", TransactionCategory.FOOD, "Dinner date 🍣", 33),
                new TxSeed("KFC Cambodia", "Wing Bank", 7.25, "USD", TransactionCategory.FOOD, "Lunch combo", 35),
                new TxSeed("Lucky Supermarket", "ABA Bank", 28.90, "USD", TransactionCategory.SHOPPING, "Groceries", 34),
                new TxSeed("Miniso", "ABA Bank", 15.00, "USD", TransactionCategory.SHOPPING, "Phone case + cable", 36),
                new TxSeed("Grab Cambodia", "Wing Bank", 4.00, "USD", TransactionCategory.TRANSPORT, "Ride to TTP", 31),
                new TxSeed("Total Gas Station", "ABA Bank", 20.00, "USD", TransactionCategory.TRANSPORT, "Fuel ⛽", 37),
                new TxSeed("EDC Electricity", "ABA Bank", 25.00, "USD", TransactionCategory.UTILITIES, "Feb electricity", 38),
                new TxSeed("Royal Phnom Penh Hospital", "ABA Bank", 35.00, "USD", TransactionCategory.HEALTH, "Health checkup", 33),
                new TxSeed("Dara", "Wing Bank", 30.00, "USD", TransactionCategory.TRANSFER, "Birthday gift 🎂", 35),
                new TxSeed("Steam Games", "ABA Bank", 14.99, "USD", TransactionCategory.ENTERTAINMENT, "New game purchase 🎮", 31),
                new TxSeed("Udemy Course", "ABA Bank", 12.99, "USD", TransactionCategory.EDUCATION, "React course", 39),

                // ---- 2 months ago ----
                new TxSeed("Meat & Drink", "Wing Bank", 22.00, "USD", TransactionCategory.FOOD, "Team dinner BBQ 🥩", 62),
                new TxSeed("Brown Coffee", "ABA Bank", 5.00, "USD", TransactionCategory.FOOD, "Large cold brew", 63),
                new TxSeed("Lucky Supermarket", "ABA Bank", 35.60, "USD", TransactionCategory.SHOPPING, "Monthly groceries", 64),
                new TxSeed("ZandoMall", "Wing Bank", 55.00, "USD", TransactionCategory.SHOPPING, "Jacket + jeans", 65),
                new TxSeed("Grab Cambodia", "Wing Bank", 5.50, "USD", TransactionCategory.TRANSPORT, "Late night ride", 61),
                new TxSeed("EDC Electricity", "ABA Bank", 30.00, "USD", TransactionCategory.UTILITIES, "Jan electricity", 66),
                new TxSeed("Smart Axiata", "Wing Bank", 10.00, "USD", TransactionCategory.UTILITIES, "Phone top-up", 63),
                new TxSeed("Mom", "Wing Bank", 100.00, "USD", TransactionCategory.TRANSFER, "Monthly support 💕", 60),
                new TxSeed("Cinema Legend", "ABA Bank", 8.00, "USD", TransactionCategory.ENTERTAINMENT, "Movie night 🎬", 67),

                // ---- KHR transactions ----
                new TxSeed("Phsar Thmey Market", "Wing Bank", 40000, "KHR", TransactionCategory.FOOD, "Fresh fruits 🍉", 5),
                new TxSeed("Tuk Tuk Driver", "Wing Bank", 8000, "KHR", TransactionCategory.TRANSPORT, "Short ride", 8),
                new TxSeed("Local Barber", "Wing Bank", 12000, "KHR", TransactionCategory.OTHER, "Haircut 💇", 10),
                new TxSeed("Night Market Food", "Wing Bank", 15000, "KHR", TransactionCategory.FOOD, "Street food night", 14),
                new TxSeed("Laundry Service", "Wing Bank", 20000, "KHR", TransactionCategory.OTHER, "Dry cleaning", 18)
        );

        // Suspicious/blocked transaction
        Transaction blocked = buildTx(user, "Unknown Merchant X", "Unknown Bank", 500.00, "USD",
                TransactionCategory.OTHER, RiskLevel.HIGH_RISK, PaymentContext.MERCHANT,
                TransactionStatus.BLOCKED, "⚠️ Suspicious payment detected", now.minusDays(10));
        transactionRepository.save(blocked);

        // Verified transaction
        Transaction verified = buildTx(user, "WingShop Official", "Wing Bank", 75.00, "USD",
                TransactionCategory.SHOPPING, RiskLevel.SAFE, PaymentContext.WINGSHOP,
                TransactionStatus.VERIFIED, "Verified WingShop purchase ✅", now.minusDays(3));
        transactionRepository.save(verified);

        for (TxSeed s : seeds) {
            Transaction tx = buildTx(user, s.recipient, s.bank, s.amount, s.currency,
                    s.category, RiskLevel.SAFE, PaymentContext.MERCHANT,
                    TransactionStatus.PAID, s.note, now.minusDays(s.daysAgo));
            transactionRepository.save(tx);
        }

        log.info("   ✅ {} transactions seeded", seeds.size() + 2);

        // ── 3. Seed savings goals ────────────────────────────────────────────
        SavingGoal goal1 = SavingGoal.builder()
                .user(user)
                .title("Emergency Fund")
                .targetAmount(new BigDecimal("1000.00"))
                .currentAmount(new BigDecimal("650.00"))
                .currency("USD")
                .emoji("🏦")
                .deadline(LocalDate.of(2026, 8, 1))
                .status(SavingGoalStatus.ACTIVE)
                .build();

        SavingGoal goal2 = SavingGoal.builder()
                .user(user)
                .title("MacBook Pro")
                .targetAmount(new BigDecimal("1500.00"))
                .currentAmount(new BigDecimal("420.00"))
                .currency("USD")
                .emoji("💻")
                .deadline(LocalDate.of(2026, 12, 31))
                .status(SavingGoalStatus.ACTIVE)
                .build();

        SavingGoal goal3 = SavingGoal.builder()
                .user(user)
                .title("Siem Reap Trip")
                .targetAmount(new BigDecimal("200.00"))
                .currentAmount(new BigDecimal("200.00"))
                .currency("USD")
                .emoji("✈️")
                .deadline(LocalDate.of(2026, 5, 15))
                .status(SavingGoalStatus.COMPLETED)
                .build();

        SavingGoal goal4 = SavingGoal.builder()
                .user(user)
                .title("New Phone")
                .targetAmount(new BigDecimal("800.00"))
                .currentAmount(new BigDecimal("125.00"))
                .currency("USD")
                .emoji("📱")
                .deadline(LocalDate.of(2027, 3, 1))
                .status(SavingGoalStatus.ACTIVE)
                .build();

        savingGoalRepository.saveAll(List.of(goal1, goal2, goal3, goal4));
        log.info("   ✅ 4 savings goals seeded");

        log.info("🎉 Demo data seeding complete! Login with phone: {} password: 123456", DEMO_PHONE);
    }

    private Transaction buildTx(User user, String recipient, String bank,
                                double amount, String currency, TransactionCategory category,
                                RiskLevel risk, PaymentContext context,
                                TransactionStatus status, String note, LocalDateTime createdAt) {
        Transaction tx = Transaction.builder()
                .user(user)
                .recipientName(recipient)
                .bankName(bank)
                .amount(BigDecimal.valueOf(amount))
                .currency(currency)
                .category(category)
                .riskLevel(risk)
                .paymentContext(context)
                .status(status)
                .note(note)
                .build();
        // Manually set createdAt for historical dates
        tx.setCreatedAt(createdAt);
        return tx;
    }

    private record TxSeed(String recipient, String bank, double amount, String currency,
                           TransactionCategory category, String note, int daysAgo) {}
}
