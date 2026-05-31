--liquibase formatted sql

--changeset dev:006-seed-data
INSERT INTO promotions (type, value, active) VALUES
    ('PERCENTAGE_DISCOUNT', 10.0, true),
    ('BUY2_GET1_FREE',      null, true),
    ('VIP_DISCOUNT',        5.0,  true);

INSERT INTO coupons (code, discount_amount, active, expiry_date) VALUES
    ('SUMMER10', 10.00, true, '2099-12-31'),
    ('SAVE20',   20.00, true, '2099-12-31');
