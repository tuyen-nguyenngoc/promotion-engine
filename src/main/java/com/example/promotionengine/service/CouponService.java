package com.example.promotionengine.service;

import com.example.promotionengine.dto.request.CouponCreateRequest;
import com.example.promotionengine.dto.response.CouponResponse;

import java.util.List;

public interface CouponService {
    List<CouponResponse> getAllCoupons();

    CouponResponse createCoupon(CouponCreateRequest request);

    CouponResponse deactivateCoupon(String code);
}
