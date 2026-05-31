package com.example.promotionengine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse {

    private String code;
    private BigDecimal discountAmount;
    private Boolean active;
    private LocalDate expiryDate;
    private OffsetDateTime createdAt;
}
