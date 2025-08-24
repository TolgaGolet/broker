package com.brokagefirm.broker.service;

import com.brokagefirm.broker.api.request.GetCustomerOrdersRequest;
import com.brokagefirm.broker.api.request.OrderCreateRequest;
import com.brokagefirm.broker.api.response.OrderResponse;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.service.dto.OrderDto;
import org.springframework.data.domain.Page;

public interface OrderService {
    OrderDto createOrder(OrderCreateRequest orderCreateRequest) throws BrokerGenericException;

    Page<OrderResponse> getCustomerOrders(Long customerId, GetCustomerOrdersRequest request, int pageNo) throws BrokerGenericException;

    OrderDto cancelOrder(Long orderId) throws BrokerGenericException;

    OrderDto matchOrder(Long orderId) throws BrokerGenericException;
}
