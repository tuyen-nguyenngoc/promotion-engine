package com.example.promotionengine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponse {

    private Long id;
    private String type;
    private BigDecimal value;
    private Boolean active;
    private OffsetDateTime createdAt;
}
