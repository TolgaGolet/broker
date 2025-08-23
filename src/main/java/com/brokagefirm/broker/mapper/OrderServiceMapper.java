package com.brokagefirm.broker.mapper;

import com.brokagefirm.broker.api.request.OrderCreateRequest;
import com.brokagefirm.broker.api.response.OrderResponse;
import com.brokagefirm.broker.entity.Order;
import com.brokagefirm.broker.service.dto.OrderDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(componentModel = "spring", unmappedTargetPolicy = IGNORE)
public interface OrderServiceMapper {
    @Mapping(source = "customer.id", target = "customerId")
    OrderResponse toOrderResponse(OrderDto orderDto);

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "side", target = "orderSide")
    @Mapping(source = "status", target = "orderStatus")
    OrderResponse toOrderResponse(Order order);

    Order toOrder(OrderCreateRequest orderCreateRequest);

    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "side", target = "orderSide")
    @Mapping(source = "status", target = "orderStatus")
    OrderDto toOrderDto(Order order);
}
