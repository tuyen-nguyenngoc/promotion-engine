package com.example.promotionengine.service;

import com.example.promotionengine.dto.request.OrderCalculateRequest;
import com.example.promotionengine.dto.response.OrderCalculateResponse;

public interface OrderService {
    OrderCalculateResponse calculate(OrderCalculateRequest request);
}
