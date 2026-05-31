package com.example.promotionengine.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CouponCreateRequest {

    @NotBlank(message = "code is required")
    private String code;

    @NotNull(message = "discountAmount is required")
    @DecimalMin(value = "0.01", message = "discountAmount must be greater than 0")
    private BigDecimal discountAmount;

    @NotNull(message = "active is required")
    private Boolean active;

    private LocalDate expiryDate;
}
