package com.example.promotionengine;

import com.example.promotionengine.dto.request.OrderCalculateRequest;
import com.example.promotionengine.dto.request.OrderItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class OrderCalculateIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("promotiondb")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderItemRequest item(String sku, double price, int qty) {
        OrderItemRequest r = new OrderItemRequest();
        r.setSku(sku); r.setPrice(BigDecimal.valueOf(price)); r.setQuantity(qty);
        return r;
    }

    @Test
    void calculate_fullRequest_returns102_50() throws Exception {
        OrderCalculateRequest request = new OrderCalculateRequest();
        request.setCustomerType("VIP");
        request.setItems(List.of(item("A100", 100, 2), item("B200", 50, 1)));
        request.setCouponCode("SUMMER10");

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subtotal").value(250.00))
                .andExpect(jsonPath("$.data.totalDiscount").value(147.50))
                .andExpect(jsonPath("$.data.finalPrice").value(102.50))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    void calculate_invalidCoupon_returns400() throws Exception {
        OrderCalculateRequest request = new OrderCalculateRequest();
        request.setCustomerType("VIP");
        request.setItems(List.of(item("A100", 100, 1)));
        request.setCouponCode("NONEXISTENT");

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_COUPON"));
    }

    @Test
    void calculate_regularCustomerNoCoupon_onlyPercentageAndBuy2Get1Free() throws Exception {
        OrderCalculateRequest request = new OrderCalculateRequest();
        request.setCustomerType("REGULAR");
        request.setItems(List.of(item("A100", 100, 2)));
        request.setCouponCode(null);

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                // subtotal=200, PCT=20, BUY2GET1FREE=100, total=120, final=80
                .andExpect(jsonPath("$.data.subtotal").value(200.00))
                .andExpect(jsonPath("$.data.finalPrice").value(80.00))
                .andExpect(jsonPath("$.data.discounts.length()").value(2));
    }
}
