package com.brokagefirm.broker.service;

import com.brokagefirm.broker.api.request.GetCustomerOrdersRequest;
import com.brokagefirm.broker.api.request.OrderCreateRequest;
import com.brokagefirm.broker.api.response.OrderResponse;
import com.brokagefirm.broker.entity.BrokerCustomer;
import com.brokagefirm.broker.entity.Order;
import com.brokagefirm.broker.enums.OrderSide;
import com.brokagefirm.broker.enums.OrderStatus;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.exception.enums.GenericExceptionMessages;
import com.brokagefirm.broker.mapper.OrderServiceMapper;
import com.brokagefirm.broker.repository.OrderRepository;
import com.brokagefirm.broker.service.dto.OrderDto;
import com.brokagefirm.broker.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.brokagefirm.broker.security.SecurityParams.DEFAULT_PAGE_SIZE;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderServiceMapper mapper;
    private final OrderRepository orderRepository;
    private final CustomerService customerService;

    @Override
    public OrderDto createOrder(OrderCreateRequest orderCreateRequest) throws BrokerGenericException {
        // TODO: update usable sizes. Throw exception if not enough.
        customerService.validateCustomerIdIfItsTheCurrentCustomer(orderCreateRequest.getCustomerId());

        Order order = new Order();
        if (!customerService.isCustomerExists(orderCreateRequest.getCustomerId())) {
            throw new BrokerGenericException(GenericExceptionMessages.CUSTOMER_NOT_FOUND.getMessage());
        }
        order.setCustomer(BrokerCustomer.builder().id(orderCreateRequest.getCustomerId()).build());
        order.setAssetName(orderCreateRequest.getAssetName());
        order.setSide(OrderSide.valueOf(orderCreateRequest.getOrderSideValue()));
        order.setSize(orderCreateRequest.getSize());
        order.setPrice(orderCreateRequest.getPrice());
        order.setStatus(OrderStatus.PENDING);

        return mapper.toOrderDto(orderRepository.save(order));
    }

    @Override
    public Page<OrderResponse> getCustomerOrders(Long customerId, GetCustomerOrdersRequest request) throws BrokerGenericException {
        customerService.validateCustomerIdIfItsTheCurrentCustomer(customerId);
        Specification<Order> spec = OrderSpecification.search(request, customerId);
        Page<Order> orders = orderRepository.findAll(spec, PageRequest.of(request.getPageNo(), DEFAULT_PAGE_SIZE,
                Sort.by("status").descending().and(Sort.by("createdDate").descending())));
        return orders.map(mapper::toOrderResponse);
    }

    @Override
    public OrderDto cancelOrder(Long orderId) throws BrokerGenericException {
        // TODO: update usable sizes. Throw exception if not enough.
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BrokerGenericException(GenericExceptionMessages.ORDER_NOT_FOUND.getMessage()));
        customerService.validateCustomerIdIfItsTheCurrentCustomer(order.getCustomer().getId());
        if (!Objects.equals(order.getStatus(), OrderStatus.PENDING)) {
            throw new BrokerGenericException(GenericExceptionMessages.ORDER_NOT_PENDING.getMessage());
        }
        order.setStatus(OrderStatus.CANCELLED);
        return mapper.toOrderDto(orderRepository.save(order));
    }
}
