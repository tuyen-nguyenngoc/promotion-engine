--liquibase formatted sql

--changeset dev:005-create-order-items
CREATE TABLE order_items (
    id        BIGSERIAL      PRIMARY KEY,
    order_id  BIGINT         NOT NULL REFERENCES orders(id),
    sku       VARCHAR(50)    NOT NULL,
    price     NUMERIC(12,2)  NOT NULL,
    quantity  INT            NOT NULL CHECK (quantity > 0)
);
