--liquibase formatted sql

--changeset dev:001-create-products
CREATE TABLE products (
    sku         VARCHAR(50)    PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    price       NUMERIC(12,2)  NOT NULL CHECK (price > 0),
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
