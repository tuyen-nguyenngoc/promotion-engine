package com.example.promotionengine.domain.chain;

import com.example.promotionengine.domain.model.DiscountDetail;
import com.example.promotionengine.domain.model.OrderContext;

import java.util.List;

/**
 * Chain of Responsibility — pipeline orchestrator.
 * Holds the head of the handler chain and executes it against an OrderContext.
 * The pipeline is assembled once at startup via Spring configuration.
 */
public class PromotionPipeline {

    private final PromotionHandler head;

    public PromotionPipeline(PromotionHandler head) {
        this.head = head;
    }

    public List<DiscountDetail> process(OrderContext context) {
        return head.handle(context);
    }
}
