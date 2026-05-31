package com.example.promotionengine.service;

import com.example.promotionengine.domain.chain.PromotionPipeline;
import com.example.promotionengine.domain.model.DiscountDetail;
import com.example.promotionengine.domain.model.OrderContext;
import com.example.promotionengine.dto.request.OrderCalculateRequest;
import com.example.promotionengine.dto.request.OrderItemRequest;
import com.example.promotionengine.dto.response.OrderCalculateResponse;
import com.example.promotionengine.entity.Coupon;
import com.example.promotionengine.entity.Order;
import com.example.promotionengine.entity.OrderItem;
import com.example.promotionengine.entity.Promotion;
import com.example.promotionengine.exception.BusinessException;
import com.example.promotionengine.repository.CouponRepository;
import com.example.promotionengine.repository.OrderRepository;
import com.example.promotionengine.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final PromotionRepository promotionRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final PromotionPipeline promotionPipeline;

    @Transactional
    public OrderCalculateResponse calculate(OrderCalculateRequest request) {
        // 1. Calculate subtotal
        BigDecimal subtotal = request.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Load active promotions
        List<Promotion> activePromotions = promotionRepository.findByActiveTrue();

        // 3. Load coupon if provided (validate existence — actual validation is in CouponDiscountStrategy)
        Coupon coupon = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            coupon = couponRepository.findByCode(request.getCouponCode()).orElse(null);
            if (coupon == null) {
                throw new BusinessException("INVALID_COUPON",
                        "Coupon '" + request.getCouponCode() + "' does not exist");
            }
        }

        // 4. Build context
        OrderContext context = OrderContext.builder()
                .customerType(request.getCustomerType())
                .items(request.getItems())
                .couponCode(request.getCouponCode())
                .subtotal(subtotal)
                .activePromotions(activePromotions)
                .coupon(coupon)
                .build();

        // 5. Execute pipeline
        List<DiscountDetail> discounts = promotionPipeline.process(context);

        // 6. Total discount
        BigDecimal totalDiscount = discounts.stream()
                .map(DiscountDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 7. Final price — clamp to 0
        BigDecimal finalPrice = subtotal.subtract(totalDiscount)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        // 8. Persist order
        Order order = new Order();
        order.setCustomerType(request.getCustomerType());
        order.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        order.setTotalDiscount(totalDiscount.setScale(2, RoundingMode.HALF_UP));
        order.setFinalPrice(finalPrice);

        List<OrderItem> orderItems = request.getItems().stream().map(itemReq -> {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setSku(itemReq.getSku());
            oi.setPrice(itemReq.getPrice());
            oi.setQuantity(itemReq.getQuantity());
            return oi;
        }).collect(Collectors.toList());
        order.setItems(orderItems);
        orderRepository.save(order);

        // 9. Build response
        List<OrderCalculateResponse.DiscountDetailResponse> discountResponses = discounts.stream()
                .map(d -> new OrderCalculateResponse.DiscountDetailResponse(d.getType(), d.getAmount()))
                .collect(Collectors.toList());

        return new OrderCalculateResponse(
                subtotal.setScale(2, RoundingMode.HALF_UP),
                discountResponses,
                totalDiscount.setScale(2, RoundingMode.HALF_UP),
                finalPrice
        );
    }
}
