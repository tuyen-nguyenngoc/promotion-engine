package com.example.promotionengine.repository;

import com.example.promotionengine.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, String> {
    Optional<Coupon> findByCode(String code);

    @Modifying
    @Query(value = """
            UPDATE coupons
               SET usage_count = usage_count + 1
             WHERE code = :code
               AND active = TRUE
               AND (expiry_date IS NULL OR expiry_date >= CURRENT_DATE)
               AND (max_usage IS NULL OR usage_count < max_usage)
            """, nativeQuery = true)
    int redeemIfAvailable(@Param("code") String code);
}
