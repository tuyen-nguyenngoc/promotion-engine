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

class VipDiscountStrategyTest {

    private VipDiscountStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new VipDiscountStrategy();
    }

    private Promotion vipPromotion() {
        Promotion p = new Promotion();
        p.setType("VIP_DISCOUNT");
        p.setValue(BigDecimal.valueOf(5));
        p.setActive(true);
        return p;
    }

    @Test
    void calculate_vipCustomer_returnsCorrectDiscount() {
        OrderContext context = OrderContext.builder()
                .customerType("VIP")
                .subtotal(BigDecimal.valueOf(250))
                .activePromotions(List.of(vipPromotion()))
                .build();

        Optional<DiscountDetail> result = strategy.calculate(context);

        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo("VIP_DISCOUNT");
        assertThat(result.get().getAmount()).isEqualByComparingTo("12.50");
    }

    @Test
    void calculate_regularCustomer_returnsEmpty() {
        OrderContext context = OrderContext.builder()
                .customerType("REGULAR")
                .subtotal(BigDecimal.valueOf(250))
                .activePromotions(List.of(vipPromotion()))
                .build();

        Optional<DiscountDetail> result = strategy.calculate(context);

        assertThat(result).isEmpty();
    }
}
