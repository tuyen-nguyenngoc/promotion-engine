package com.example.promotionengine.domain.strategy;

import com.example.promotionengine.domain.model.DiscountDetail;
import com.example.promotionengine.domain.model.OrderContext;
import com.example.promotionengine.entity.Promotion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Strategy Pattern implementation — applies a flat percentage discount to the subtotal.
 * Reads the percentage value from the active PERCENTAGE_DISCOUNT promotion record.
 */
@Component
public class PercentageDiscountStrategy implements PromotionStrategy {

    private static final String TYPE = "PERCENTAGE_DISCOUNT";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Optional<DiscountDetail> calculate(OrderContext context) {
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
