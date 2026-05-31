package com.example.promotionengine.domain.chain;

import com.example.promotionengine.domain.model.DiscountDetail;
import com.example.promotionengine.domain.model.OrderContext;
import com.example.promotionengine.domain.strategy.PromotionStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Chain of Responsibility — abstract handler node.
 * Each handler wraps a PromotionStrategy and delegates to the next handler in the chain.
 * The chain propagates through all nodes, collecting discount results.
 */
public abstract class PromotionHandler {

    private PromotionHandler next;

    public PromotionHandler setNext(PromotionHandler next) {
        this.next = next;
        return next;
    }

    public List<DiscountDetail> handle(OrderContext context) {
        List<DiscountDetail> results = new ArrayList<>();
        doHandle(context).ifPresent(results::add);
        if (next != null) {
            results.addAll(next.handle(context));
        }
        return results;
    }

    protected abstract Optional<DiscountDetail> doHandle(OrderContext context);
}
