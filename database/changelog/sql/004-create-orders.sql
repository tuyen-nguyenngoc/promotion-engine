--liquibase formatted sql

--changeset dev:004-create-orders
CREATE TABLE orders (
    id              BIGSERIAL      PRIMARY KEY,
    customer_type   VARCHAR(20)    NOT NULL,
    subtotal        NUMERIC(12,2)  NOT NULL,
    total_discount  NUMERIC(12,2)  NOT NULL,
    final_price     NUMERIC(12,2)  NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
