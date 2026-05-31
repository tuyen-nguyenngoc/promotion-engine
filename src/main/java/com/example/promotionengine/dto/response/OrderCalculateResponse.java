package com.example.promotionengine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCalculateResponse {

    private BigDecimal subtotal;
    private List<DiscountDetailResponse> discounts;
    private BigDecimal totalDiscount;
    private BigDecimal finalPrice;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountDetailResponse {
        private String type;
        private BigDecimal amount;
    }
}
