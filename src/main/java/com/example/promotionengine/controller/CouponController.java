package com.example.promotionengine.controller;

import com.example.promotionengine.dto.request.CouponCreateRequest;
import com.example.promotionengine.dto.response.ApiResponse;
import com.example.promotionengine.dto.response.CouponResponse;
import com.example.promotionengine.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAllCoupons() {
        return ResponseEntity.ok(ApiResponse.success(couponService.getAllCoupons()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CouponCreateRequest request) {
        CouponResponse created = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PatchMapping("/{code}/deactivate")
    public ResponseEntity<ApiResponse<CouponResponse>> deactivateCoupon(
            @PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(couponService.deactivateCoupon(code)));
    }
}
