INSERT INTO merchants (merchant_id, merchant_name, bank_name, category, province, is_verified)
VALUES
    ('KHQR002', 'Brown Coffee', 'ABA Bank', 'FOOD', 'Phnom Penh', true),
    ('SHOP001', 'WingShop Official', 'Wing Bank', 'SHOPPING', 'Phnom Penh', true),
    ('P2P001', 'Kim Sok', 'Wing Bank', 'TRANSFER', 'Phnom Penh', false)
    ON CONFLICT DO NOTHING;