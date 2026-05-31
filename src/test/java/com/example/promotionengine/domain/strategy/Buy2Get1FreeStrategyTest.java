package com.example.promotionengine.domain.strategy;

import com.example.promotionengine.domain.model.DiscountDetail;
import com.example.promotionengine.domain.model.OrderContext;
import com.example.promotionengine.dto.request.OrderItemRequest;
import com.example.promotionengine.entity.Promotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class Buy2Get1FreeStrategyTest {

    private Buy2Get1FreeStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new Buy2Get1FreeStrategy();
    }

    private Promotion activePromotion() {
        Promotion p = new Promotion();
        p.setType("BUY2_GET1_FREE");
        p.setActive(true);
        return p;
    }

    private OrderItemRequest item(String sku, double price, int qty) {
        OrderItemRequest r = new OrderItemRequest();
        r.setSku(sku);
        r.setPrice(BigDecimal.valueOf(price));
        r.setQuantity(qty);
        return r;
    }

    @Test
    void calculate_qty2price100_returns100Discount() {
        OrderContext context = OrderContext.builder()
                .items(List.of(item("A100", 100, 2)))
                .activePromotions(List.of(activePromotion()))
                .build();

        Optional<DiscountDetail> result = strategy.calculate(context);

        assertThat(result).isPresent();
        assertThat(result.get().getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void calculate_qty1_returnsEmpty() {
        OrderContext context = OrderContext.builder()
                .items(List.of(item("A100", 100, 1)))
                .activePromotions(List.of(activePromotion()))
                .build();

        Optional<DiscountDetail> result = strategy.calculate(context);

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_qty4price50_returns100Discount() {
        // floor(4/2) = 2 free units, 2 * 50 = 100
        OrderContext context = OrderContext.builder()
                .items(List.of(item("A100", 50, 4)))
                .activePromotions(List.of(activePromotion()))
                .build();

        Optional<DiscountDetail> result = strategy.calculate(context);

        assertThat(result).isPresent();
        assertThat(result.get().getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void calculate_inactivePromotion_returnsEmpty() {
        Promotion inactive = new Promotion();
        inactive.setType("BUY2_GET1_FREE");
        inactive.setActive(false);

        OrderContext context = OrderContext.builder()
                .items(List.of(item("A100", 100, 4)))
                .activePromotions(List.of(inactive))
                .build();

        Optional<DiscountDetail> result = strategy.calculate(context);

        assertThat(result).isEmpty();
    }
}
