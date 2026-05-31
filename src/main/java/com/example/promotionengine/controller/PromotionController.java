package com.example.promotionengine.controller;

import com.example.promotionengine.dto.request.PromotionCreateRequest;
import com.example.promotionengine.dto.response.ApiResponse;
import com.example.promotionengine.dto.response.PromotionResponse;
import com.example.promotionengine.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActivePromotions() {
        return ResponseEntity.ok(ApiResponse.success(promotionService.getActivePromotions()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(
            @Valid @RequestBody PromotionCreateRequest request) {
        PromotionResponse created = promotionService.createPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<PromotionResponse>> deactivatePromotion(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(promotionService.deactivatePromotion(id)));
    }
}
