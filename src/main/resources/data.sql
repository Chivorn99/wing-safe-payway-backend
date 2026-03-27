INSERT INTO merchants (merchant_id, merchant_name, bank_name, category, province, is_verified)
VALUES
    ('KHQR001', 'Lucky Supermarket', 'Wing Bank', 'retail', 'Phnom Penh', true),
    ('KHQR002', 'Brown Coffee', 'ABA Bank', 'food', 'Phnom Penh', true),
    ('KHQR003', 'Grab Cambodia', 'Wing Bank', 'transport', 'Nationwide', true),
    ('KHQR004', 'Caring Pharmacy', 'ACLEDA', 'health', 'Siem Reap', true),
    ('KHQR005', 'Aeon Mall', 'Canadia Bank', 'retail', 'Phnom Penh', true),
    ('KHQR006', 'Pizza Company', 'Wing Bank', 'food', 'Phnom Penh', true),
    ('KHQR007', 'Olympia Mall', 'ABA Bank', 'retail', 'Phnom Penh', true),
    ('KHQR008', 'PassApp Taxi', 'Wing Bank', 'transport', 'Nationwide', true)
    ON CONFLICT DO NOTHING;