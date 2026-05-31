package com.example.promotionengine.domain.chain;

import com.example.promotionengine.domain.model.DiscountDetail;
import com.example.promotionengine.domain.model.OrderContext;
import com.example.promotionengine.domain.strategy.PromotionStrategy;

import java.util.Optional;

/**
 * Concrete handler that delegates its calculation to a PromotionStrategy.
 * Bridges the Chain of Responsibility with the Strategy pattern.
 */
public class StrategyPromotionHandler extends PromotionHandler {

    private final PromotionStrategy strategy;

    public StrategyPromotionHandler(PromotionStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    protected Optional<DiscountDetail> doHandle(OrderContext context) {
        return strategy.calculate(context);
    }
}
