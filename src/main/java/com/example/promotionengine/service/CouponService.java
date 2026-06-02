package com.example.promotionengine.service;

import com.example.promotionengine.dto.request.CouponCreateRequest;
import com.example.promotionengine.dto.response.CouponResponse;
import com.example.promotionengine.entity.Coupon;
import com.example.promotionengine.exception.BusinessException;
import com.example.promotionengine.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CouponResponse createCoupon(CouponCreateRequest request) {
        String code = request.getCode();
        if (couponRepository.findByCode(code).isPresent()) {
            throw new BusinessException("COUPON_ALREADY_EXISTS",
                    "Coupon with code '" + code + "' already exists");
        }
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setDiscountAmount(request.getDiscountAmount());
        coupon.setActive(request.getActive());
        coupon.setExpiryDate(request.getExpiryDate());
        coupon.setMaxUsage(request.getMaxUsage());
        coupon.setUsageCount(0);
        return toResponse(couponRepository.save(coupon));
    }

    @Transactional
    public CouponResponse deactivateCoupon(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException("INVALID_COUPON",
                        "Coupon '" + code + "' does not exist"));
        if (!Boolean.TRUE.equals(coupon.getActive())) {
            throw new BusinessException("COUPON_ALREADY_INACTIVE",
                    "Coupon '" + code + "' is already inactive");
        }
        coupon.setActive(false);
        return toResponse(couponRepository.save(coupon));
    }

    private CouponResponse toResponse(Coupon c) {
        return new CouponResponse(
                c.getCode(), c.getDiscountAmount(), c.getActive(),
                c.getExpiryDate(), c.getMaxUsage(), c.getUsageCount(), c.getCreatedAt());
    }
}
