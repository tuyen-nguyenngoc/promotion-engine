package com.example.promotionengine.config;

import com.example.promotionengine.domain.chain.PromotionHandler;
import com.example.promotionengine.domain.chain.PromotionPipeline;
import com.example.promotionengine.domain.chain.StrategyPromotionHandler;
import com.example.promotionengine.domain.strategy.Buy2Get1FreeStrategy;
import com.example.promotionengine.domain.strategy.CouponDiscountStrategy;
import com.example.promotionengine.domain.strategy.PercentageDiscountStrategy;
import com.example.promotionengine.domain.strategy.VipDiscountStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the Chain of Responsibility pipeline.
 * Order: Percentage → Buy2Get1Free → VIP → Coupon
 * All discounts are calculated independently against the original subtotal.
 */
@Configuration
public class PromotionPipelineConfig {

    @Bean
    public PromotionPipeline promotionPipeline(
            PercentageDiscountStrategy percentageDiscountStrategy,
            Buy2Get1FreeStrategy buy2Get1FreeStrategy,
            VipDiscountStrategy vipDiscountStrategy,
            CouponDiscountStrategy couponDiscountStrategy) {

        PromotionHandler head = new StrategyPromotionHandler(percentageDiscountStrategy);
        head.setNext(new StrategyPromotionHandler(vipDiscountStrategy))
            .setNext(new StrategyPromotionHandler(couponDiscountStrategy))
            .setNext(new StrategyPromotionHandler(buy2Get1FreeStrategy));

        return new PromotionPipeline(head);
    }
}
