--liquibase formatted sql

--changeset dev:007-add-coupon-usage-limits
ALTER TABLE coupons
    ADD COLUMN max_usage   INTEGER CHECK (max_usage IS NULL OR max_usage > 0),
    ADD COLUMN usage_count INTEGER NOT NULL DEFAULT 0 CHECK (usage_count >= 0);

CREATE INDEX idx_coupons_active_expiry
    ON coupons(active, expiry_date);

UPDATE coupons
   SET max_usage = 1
 WHERE code = 'SAVE20';
