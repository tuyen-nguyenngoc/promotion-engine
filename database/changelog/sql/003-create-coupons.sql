--liquibase formatted sql

--changeset dev:003-create-coupons
CREATE TABLE coupons (
    code             VARCHAR(50)    PRIMARY KEY,
    discount_amount  NUMERIC(12,2)  NOT NULL CHECK (discount_amount > 0),
    active           BOOLEAN        NOT NULL DEFAULT TRUE,
    expiry_date      DATE,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
