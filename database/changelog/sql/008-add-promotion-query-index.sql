--liquibase formatted sql

--changeset dev:008-add-promotion-query-index
CREATE INDEX idx_promotions_active_type
    ON promotions(active, type);
