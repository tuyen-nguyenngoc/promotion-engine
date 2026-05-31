package com.example.promotionengine.domain.strategy;

import com.example.promotionengine.domain.model.DiscountDetail;
import com.example.promotionengine.domain.model.OrderContext;
import com.example.promotionengine.entity.Promotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PercentageDiscountStrategyTest {

    private PercentageDiscountStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PercentageDiscountStrategy();
    }

    private Promotion activePromotion(String type, double value) {
        Promotion p = new Promotion();
        p.setType(type);
        p.setValue(BigDecimal.valueOf(value));
        p.setActive(true);
        return p;
    }

    private Promotion inactivePromotion(String type, double value) {
        Promotion p = new Promotion();
        p.setType(type);
        p.setValue(BigDecimal.valueOf(value));
        p.setActive(false);
        return p;
    }

    @Test
    void calculate_withActivePromotion_returnsCorrectDiscount() {
        OrderContext context = OrderContext.builder()
                .subtotal(BigDecimal.valueOf(250))
                .activePromotions(List.of(activePromotion("PERCENTAGE_DISCOUNT", 10)))
                .build();

        Optional<DiscountDetail> result = strategy.calculate(context);

        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo("PERCENTAGE_DISCOUNT");
        assertThat(result.get().getAmount()).isEqualByComparingTo("25.00");
    }

    @Test
    void calculate_withInactivePromotion_returnsEmpty() {
        OrderContext context = OrderContext.builder()
                .subtotal(BigDecimal.valueOf(250))
                .activePromotions(List.of(inactivePromotion("PERCENTAGE_DISCOUNT", 10)))
                .build();

        Optional<DiscountDetail> result = strategy.calculate(context);

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_withNoMatchingPromotion_returnsEmpty() {
        OrderContext context = OrderContext.builder()
                .subtotal(BigDecimal.valueOf(250))
                .activePromotions(List.of())
                .build();

        Optional<DiscountDetail> result = strategy.calculate(context);

        assertThat(result).isEmpty();
    }
}
