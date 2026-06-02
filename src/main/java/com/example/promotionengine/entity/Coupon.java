package com.example.promotionengine.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (usageCount == null) usageCount = 0;
    }
}
