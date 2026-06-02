package com.example.promotionengine.service;

import com.example.promotionengine.domain.chain.PromotionPipeline;
import com.example.promotionengine.domain.model.DiscountDetail;
import com.example.promotionengine.dto.request.OrderCalculateRequest;
import com.example.promotionengine.dto.request.OrderItemRequest;
import com.example.promotionengine.dto.response.OrderCalculateResponse;
import com.example.promotionengine.entity.Coupon;
import com.example.promotionengine.entity.Order;
import com.example.promotionengine.entity.Promotion;
import com.example.promotionengine.exception.BusinessException;
import com.example.promotionengine.repository.CouponRepository;
import com.example.promotionengine.repository.OrderRepository;
import com.example.promotionengine.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private PromotionRepository promotionRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private PromotionPipeline promotionPipeline;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(promotionRepository, couponRepository, orderRepository, promotionPipeline);
    }

    private OrderItemRequest item(String sku, double price, int qty) {
        OrderItemRequest r = new OrderItemRequest();
        r.setSku(sku); r.setPrice(BigDecimal.valueOf(price)); r.setQuantity(qty);
        return r;
    }

    @Test
    void calculate_validRequestWithCoupon_persistsAndReturnsResponse() {
        OrderCalculateRequest request = new OrderCalculateRequest();
        request.setCustomerType("VIP");
        request.setItems(List.of(item("A100", 100, 2), item("B200", 50, 1)));
        request.setCouponCode("SUMMER10");

        when(promotionRepository.findByActiveTrue()).thenReturn(List.of());
        Coupon coupon = new Coupon();
        coupon.setCode("SUMMER10");
        coupon.setDiscountAmount(BigDecimal.valueOf(10));
        coupon.setActive(true);
        coupon.setExpiryDate(LocalDate.of(2099, 12, 31));
        when(couponRepository.findByCode("SUMMER10")).thenReturn(Optional.of(coupon));
        when(couponRepository.redeemIfAvailable("SUMMER10")).thenReturn(1);

        List<DiscountDetail> mockDiscounts = List.of(
                new DiscountDetail("PERCENTAGE_DISCOUNT", BigDecimal.valueOf(25)),
                new DiscountDetail("VIP_DISCOUNT", BigDecimal.valueOf(12.5)),
                new DiscountDetail("COUPON_SUMMER10", BigDecimal.valueOf(10)),
                new DiscountDetail("BUY2_GET1_FREE", BigDecimal.valueOf(100))
        );
        when(promotionPipeline.process(any())).thenReturn(mockDiscounts);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderCalculateResponse response = orderService.calculate(request);

        assertThat(response.getSubtotal()).isEqualByComparingTo("250.00");
        assertThat(response.getTotalDiscount()).isEqualByComparingTo("147.50");
        assertThat(response.getFinalPrice()).isEqualByComparingTo("102.50");
        verify(orderRepository).save(any(Order.class));
        verify(couponRepository).redeemIfAvailable("SUMMER10");
    }

    @Test
    void calculate_invalidCoupon_throwsBusinessException() {
        OrderCalculateRequest request = new OrderCalculateRequest();
        request.setCustomerType("VIP");
        request.setItems(List.of(item("A100", 100, 1)));
        request.setCouponCode("INVALID");

        when(promotionRepository.findByActiveTrue()).thenReturn(List.of());
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.calculate(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("INVALID_COUPON");
        verify(couponRepository, never()).redeemIfAvailable(any());
    }

    @Test
    void calculate_couponUsageLimitReached_throwsBusinessException() {
        OrderCalculateRequest request = new OrderCalculateRequest();
        request.setCustomerType("VIP");
        request.setItems(List.of(item("A100", 100, 1)));
        request.setCouponCode("SAVE20");

        when(promotionRepository.findByActiveTrue()).thenReturn(List.of());
        Coupon coupon = new Coupon();
        coupon.setCode("SAVE20");
        coupon.setDiscountAmount(BigDecimal.valueOf(20));
        coupon.setActive(true);
        coupon.setExpiryDate(LocalDate.of(2099, 12, 31));
        when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(coupon));
        when(promotionPipeline.process(any())).thenReturn(List.of(
                new DiscountDetail("COUPON_SAVE20", BigDecimal.valueOf(20))
        ));
        when(couponRepository.redeemIfAvailable("SAVE20")).thenReturn(0);

        assertThatThrownBy(() -> orderService.calculate(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("COUPON_USAGE_LIMIT_REACHED");
        verify(orderRepository, never()).save(any());
    }
}
