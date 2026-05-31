package com.example.promotionengine.domain.strategy;

import com.example.promotionengine.domain.model.DiscountDetail;
import com.example.promotionengine.domain.model.OrderContext;
import com.example.promotionengine.entity.Coupon;
import com.example.promotionengine.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponDiscountStrategyTest {

    private CouponDiscountStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new CouponDiscountStrategy();
    }

    private Coupon validCoupon(String code, double amount) {
        Coupon c = new Coupon();
        c.setCode(code);
        c.setDiscountAmount(BigDecimal.valueOf(amount));
        c.setActive(true);
        c.setExpiryDate(LocalDate.of(2099, 12, 31));
        return c;
    }

    @Test
    void calculate_summer10Coupon_returns10Discount() {
        OrderContext context = OrderContext.builder()
                .couponCode("SUMMER10")
                .coupon(validCoupon("SUMMER10", 10))
                .activePromotions(List.of())
                .build();

        Optional<DiscountDetail> result = strategy.calculate(context);

        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo("COUPON_SUMMER10");
        assertThat(result.get().getAmount()).isEqualByComparingTo("10.00");
    }

    @Test
    void calculate_save20Coupon_returns20Discount() {
        OrderContext context = OrderContext.builder()
                .couponCode("SAVE20")
                .coupon(validCoupon("SAVE20", 20))
                .activePromotions(List.of())
                .build();

        Optional<DiscountDetail> result = strategy.calculate(context);

        assertThat(result).isPresent();
        assertThat(result.get().getAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void calculate_expiredCoupon_throwsBusinessException() {
        Coupon expired = validCoupon("OLD10", 10);
        expired.setExpiryDate(LocalDate.of(2000, 1, 1));

        OrderContext context = OrderContext.builder()
                .couponCode("OLD10")
                .coupon(expired)
                .activePromotions(List.of())
                .build();

        assertThatThrownBy(() -> strategy.calculate(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expired")
                .extracting("code").isEqualTo("INVALID_COUPON");
    }

    @Test
    void calculate_inactiveCoupon_throwsBusinessException() {
        Coupon inactive = validCoupon("INACTIVE", 10);
        inactive.setActive(false);

        OrderContext context = OrderContext.builder()
                .couponCode("INACTIVE")
                .coupon(inactive)
                .activePromotions(List.of())
                .build();

        assertThatThrownBy(() -> strategy.calculate(context))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("INVALID_COUPON");
    }

    @Test
    void calculate_noCouponCode_returnsEmpty() {
        OrderContext context = OrderContext.builder()
                .couponCode(null)
                .activePromotions(List.of())
                .build();

        Optional<DiscountDetail> result = strategy.calculate(context);

        assertThat(result).isEmpty();
    }
}
