package com.example.promotionengine.domain.strategy;

import com.example.promotionengine.domain.model.DiscountDetail;
import com.example.promotionengine.domain.model.OrderContext;
import com.example.promotionengine.dto.request.OrderItemRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Strategy Pattern implementation — for every 2 units of the same SKU, 1 unit is free.
 * Formula: freeUnits = floor(qty / 2), discount = freeUnits × unitPrice (per SKU, summed).
 */
@Component
public class Buy2Get1FreeStrategy implements PromotionStrategy {

    private static final String TYPE = "BUY2_GET1_FREE";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Optional<DiscountDetail> calculate(OrderContext context) {
        boolean active = context.getActivePromotions().stream()
                .anyMatch(p -> TYPE.equals(p.getType()) && Boolean.TRUE.equals(p.getActive()));
        if (!active) return Optional.empty();

        BigDecimal totalDiscount = context.getItems().stream()
                .map(item -> {
                    int freeUnits = item.getQuantity() / 2;
                    return item.getPrice().multiply(BigDecimal.valueOf(freeUnits));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDiscount.compareTo(BigDecimal.ZERO) == 0) return Optional.empty();

        return Optional.of(DiscountDetail.builder()
                .type(TYPE)
                .amount(totalDiscount)
                .build());
    }
}
