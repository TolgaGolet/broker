package com.brokagefirm.broker.service.dto;

import com.brokagefirm.broker.entity.BrokerCustomer;
import com.brokagefirm.broker.enums.OrderSide;
import com.brokagefirm.broker.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDto {
    private Long orderId;
    private BrokerCustomer customer;
    private String assetName;
    private OrderSide orderSide;
    private BigDecimal size;
    private BigDecimal price;
    private OrderStatus orderStatus;
    private LocalDateTime createdDate;
}
