package com.example.promotionengine.domain.chain;

import com.example.promotionengine.domain.model.DiscountDetail;
import com.example.promotionengine.domain.model.OrderContext;
import com.example.promotionengine.domain.strategy.*;
import com.example.promotionengine.dto.request.OrderItemRequest;
import com.example.promotionengine.entity.Coupon;
import com.example.promotionengine.entity.Promotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionPipelineTest {

    private PromotionPipeline pipeline;

    @BeforeEach
    void setUp() {
        PercentageDiscountStrategy pct = new PercentageDiscountStrategy();
        Buy2Get1FreeStrategy b2g1 = new Buy2Get1FreeStrategy();
        VipDiscountStrategy vip = new VipDiscountStrategy();
        CouponDiscountStrategy coupon = new CouponDiscountStrategy();

        PromotionHandler head = new StrategyPromotionHandler(pct);
        head.setNext(new StrategyPromotionHandler(vip))
            .setNext(new StrategyPromotionHandler(coupon))
            .setNext(new StrategyPromotionHandler(b2g1));

        pipeline = new PromotionPipeline(head);
    }

    @Test
    void process_allRulesVipSummer10_correctTotals() {
        // subtotal: A100 qty=2 price=100 + B200 qty=1 price=50 = 250
        OrderItemRequest itemA = new OrderItemRequest();
        itemA.setSku("A100"); itemA.setPrice(BigDecimal.valueOf(100)); itemA.setQuantity(2);
        OrderItemRequest itemB = new OrderItemRequest();
        itemB.setSku("B200"); itemB.setPrice(BigDecimal.valueOf(50)); itemB.setQuantity(1);

        Promotion pct = new Promotion(); pct.setType("PERCENTAGE_DISCOUNT"); pct.setValue(BigDecimal.valueOf(10)); pct.setActive(true);
        Promotion b2g1 = new Promotion(); b2g1.setType("BUY2_GET1_FREE"); b2g1.setActive(true);
        Promotion vip = new Promotion(); vip.setType("VIP_DISCOUNT"); vip.setValue(BigDecimal.valueOf(5)); vip.setActive(true);

        Coupon coupon = new Coupon();
        coupon.setCode("SUMMER10");
        coupon.setDiscountAmount(BigDecimal.valueOf(10));
        coupon.setActive(true);
        coupon.setExpiryDate(LocalDate.of(2099, 12, 31));

        OrderContext context = OrderContext.builder()
                .customerType("VIP")
                .items(List.of(itemA, itemB))
                .couponCode("SUMMER10")
                .subtotal(BigDecimal.valueOf(250))
                .activePromotions(List.of(pct, b2g1, vip))
                .coupon(coupon)
                .build();

        List<DiscountDetail> discounts = pipeline.process(context);

        BigDecimal total = discounts.stream()
                .map(DiscountDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // PERCENTAGE=25, VIP=12.5, COUPON=10, BUY2GET1FREE=100 → total=147.5
        assertThat(total).isEqualByComparingTo("147.50");
        assertThat(discounts).hasSize(4);

        BigDecimal finalPrice = BigDecimal.valueOf(250).subtract(total);
        assertThat(finalPrice).isEqualByComparingTo("102.50");
    }
}
