package com.example.promotionengine.config;

import com.example.promotionengine.domain.chain.PromotionHandler;
import com.example.promotionengine.domain.chain.PromotionPipeline;
import com.example.promotionengine.domain.chain.StrategyPromotionHandler;
import com.example.promotionengine.domain.strategy.PromotionStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Wires the Chain of Responsibility pipeline.
 * Order is controlled by @Order on each PromotionStrategy bean.
 * All discounts are calculated independently against the original subtotal.
 */
@Configuration
public class PromotionPipelineConfig {

    @Bean
    public PromotionPipeline promotionPipeline(List<PromotionStrategy> strategies) {
        if (strategies.isEmpty()) {
            throw new IllegalStateException("At least one PromotionStrategy is required");
        }

        PromotionHandler head = new StrategyPromotionHandler(strategies.get(0));
        PromotionHandler current = head;
        for (int i = 1; i < strategies.size(); i++) {
            current = current.setNext(new StrategyPromotionHandler(strategies.get(i)));
        }

        return new PromotionPipeline(head);
    }
}
