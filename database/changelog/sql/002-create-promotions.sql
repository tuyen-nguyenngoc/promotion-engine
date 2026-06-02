--liquibase formatted sql

--changeset dev:002-create-promotions
CREATE TABLE promotions (
    id          BIGSERIAL      PRIMARY KEY,
    type        VARCHAR(50)    NOT NULL,
    value       NUMERIC(10,4),
    active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
