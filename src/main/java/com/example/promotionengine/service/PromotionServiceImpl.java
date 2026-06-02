package com.example.promotionengine.service;

import com.example.promotionengine.dto.request.PromotionCreateRequest;
import com.example.promotionengine.dto.response.PromotionResponse;
import com.example.promotionengine.entity.Promotion;
import com.example.promotionengine.exception.BusinessException;
import com.example.promotionengine.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    @Transactional(readOnly = true)
    @Override
    public List<PromotionResponse> getActivePromotions() {
        return promotionRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public PromotionResponse createPromotion(PromotionCreateRequest request) {
        Promotion promotion = new Promotion();
        promotion.setType(request.getType());
        promotion.setValue(request.getValue());
        promotion.setActive(request.getActive());
        Promotion saved = promotionRepository.save(promotion);
        return toResponse(saved);
    }

    @Transactional
    @Override
    public PromotionResponse deactivatePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PROMOTION_NOT_FOUND",
                        "Promotion with id " + id + " not found"));
        if (!Boolean.TRUE.equals(promotion.getActive())) {
            throw new BusinessException("PROMOTION_ALREADY_INACTIVE",
                    "Promotion with id " + id + " is already inactive");
        }
        promotion.setActive(false);
        return toResponse(promotionRepository.save(promotion));
    }

    private PromotionResponse toResponse(Promotion p) {
        return new PromotionResponse(p.getId(), p.getType(), p.getValue(), p.getActive(), p.getCreatedAt());
    }
}
