package com.example.promotionengine.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OrderCalculateRequest {

    @NotBlank(message = "customerType is required")
    @Pattern(regexp = "VIP|REGULAR", message = "customerType must be VIP or REGULAR")
    private String customerType;

    @NotEmpty(message = "items must not be empty")
    @Valid
    private List<OrderItemRequest> items;

    private String couponCode;
}
