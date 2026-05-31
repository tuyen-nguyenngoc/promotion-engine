package com.example.promotionengine.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PromotionCreateRequest {

    @NotBlank(message = "type is required")
    @Pattern(regexp = "PERCENTAGE_DISCOUNT|BUY2_GET1_FREE|VIP_DISCOUNT",
             message = "type must be PERCENTAGE_DISCOUNT, BUY2_GET1_FREE, or VIP_DISCOUNT")
    private String type;

    private BigDecimal value;

    @NotNull(message = "active is required")
    private Boolean active;
}
