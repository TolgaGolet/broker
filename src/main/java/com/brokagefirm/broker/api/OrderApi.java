package com.brokagefirm.broker.api;

import com.brokagefirm.broker.api.request.GetCustomerOrdersRequest;
import com.brokagefirm.broker.api.request.OrderCreateRequest;
import com.brokagefirm.broker.api.response.OrderResponse;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.mapper.OrderServiceMapper;
import com.brokagefirm.broker.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/order")
@SecurityRequirement(name = "BearerAuth")
public class OrderApi {
    private final OrderService orderService;
    private final OrderServiceMapper orderServiceMapper;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Validated OrderCreateRequest orderCreateRequest) throws BrokerGenericException {
        OrderResponse response = orderServiceMapper.toOrderResponse(orderService.createOrder(orderCreateRequest));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderResponse>> getCustomerOrders(@PathVariable Long customerId, @ModelAttribute @Validated GetCustomerOrdersRequest request) throws BrokerGenericException {
        return ResponseEntity.ok(orderService.getCustomerOrders(customerId, request));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) throws BrokerGenericException {
        OrderResponse response = orderServiceMapper.toOrderResponse(orderService.cancelOrder(orderId));
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }
}
