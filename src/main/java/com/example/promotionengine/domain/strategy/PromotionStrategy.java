package com.example.promotionengine.domain.strategy;

import com.example.promotionengine.domain.model.DiscountDetail;
import com.example.promotionengine.domain.model.OrderContext;

import java.util.Optional;

/**
 * Strategy Pattern — each promotion rule is a separate strategy implementation.
 * Adding a new rule requires only creating a new class that implements this interface
 * without modifying any existing code (OCP).
 */
public interface PromotionStrategy {

    /**
     * Returns the promotion type constant this strategy handles.
     */
    String getType();

    /**
     * Calculates the discount for the given order context.
     * Returns empty if the rule does not apply.
     */
    Optional<DiscountDetail> calculate(OrderContext context);
}
