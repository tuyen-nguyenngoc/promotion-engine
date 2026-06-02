package com.example.promotionengine.service;

import com.example.promotionengine.dto.request.PromotionCreateRequest;
import com.example.promotionengine.dto.response.PromotionResponse;

import java.util.List;

public interface PromotionService {
    List<PromotionResponse> getActivePromotions();

    PromotionResponse createPromotion(PromotionCreateRequest request);

    PromotionResponse deactivatePromotion(Long id);
}
