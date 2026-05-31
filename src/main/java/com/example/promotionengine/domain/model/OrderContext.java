package com.example.promotionengine.domain.model;

import com.example.promotionengine.dto.request.OrderItemRequest;
import com.example.promotionengine.entity.Coupon;
import com.example.promotionengine.entity.Promotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderContext {
    private String customerType;
    private List<OrderItemRequest> items;
    private String couponCode;
    private BigDecimal subtotal;
    private List<Promotion> activePromotions;
    private Coupon coupon; // nullable
}
