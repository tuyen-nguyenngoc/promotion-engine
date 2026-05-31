package com.example.promotionengine.domain.strategy;

import com.example.promotionengine.domain.model.DiscountDetail;
import com.example.promotionengine.domain.model.OrderContext;
import com.example.promotionengine.entity.Coupon;
import com.example.promotionengine.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Strategy Pattern implementation — applies a flat coupon discount.
 * Validates that the coupon exists, is active, and has not expired.
 * Throws BusinessException(INVALID_COUPON) for invalid coupons (fail-fast behaviour).
 */
@Component
public class CouponDiscountStrategy implements PromotionStrategy {

    private static final String TYPE = "COUPON";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Optional<DiscountDetail> calculate(OrderContext context) {
        if (context.getCouponCode() == null || context.getCouponCode().isBlank()) {
            return Optional.empty();
        }

        Coupon coupon = context.getCoupon();
        if (coupon == null) {
            throw new BusinessException("INVALID_COUPON",
                    "Coupon '" + context.getCouponCode() + "' does not exist");
        }
        if (!Boolean.TRUE.equals(coupon.getActive())) {
            throw new BusinessException("INVALID_COUPON",
                    "Coupon '" + coupon.getCode() + "' is inactive");
        }
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BusinessException("INVALID_COUPON",
                    "Coupon '" + coupon.getCode() + "' has expired");
        }

        String discountType = "COUPON_" + coupon.getCode().toUpperCase();
        return Optional.of(DiscountDetail.builder()
                .type(discountType)
                .amount(coupon.getDiscountAmount())
                .build());
    }
}
