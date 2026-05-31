package com.example.promotionengine.domain.strategy;

import com.example.promotionengine.domain.model.DiscountDetail;
import com.example.promotionengine.domain.model.OrderContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Strategy Pattern implementation — applies an extra percentage discount for VIP customers only.
 */
@Component
public class VipDiscountStrategy implements PromotionStrategy {

    private static final String TYPE = "VIP_DISCOUNT";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Optional<DiscountDetail> calculate(OrderContext context) {
        if (!"VIP".equalsIgnoreCase(context.getCustomerType())) return Optional.empty();

        return context.getActivePromotions().stream()
                .filter(p -> TYPE.equals(p.getType()) && Boolean.TRUE.equals(p.getActive()))
                .findFirst()
                .map(p -> {
                    BigDecimal discount = context.getSubtotal()
                            .multiply(p.getValue())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    return DiscountDetail.builder()
                            .type(TYPE)
                            .amount(discount)
                            .build();
                });
    }
}
