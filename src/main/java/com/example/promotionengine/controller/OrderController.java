package com.example.promotionengine.controller;

import com.example.promotionengine.dto.request.OrderCalculateRequest;
import com.example.promotionengine.dto.response.ApiResponse;
import com.example.promotionengine.dto.response.OrderCalculateResponse;
import com.example.promotionengine.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<OrderCalculateResponse>> calculate(
            @Valid @RequestBody OrderCalculateRequest request) {
        OrderCalculateResponse response = orderService.calculate(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
