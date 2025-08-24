package com.brokagefirm.broker.service;

import com.brokagefirm.broker.api.request.AssetCreateRequest;
import com.brokagefirm.broker.api.request.AssetUpdateRequest;
import com.brokagefirm.broker.api.request.GetCustomerOrdersRequest;
import com.brokagefirm.broker.api.request.OrderCreateRequest;
import com.brokagefirm.broker.api.response.OrderResponse;
import com.brokagefirm.broker.entity.BrokerCustomer;
import com.brokagefirm.broker.entity.Order;
import com.brokagefirm.broker.enums.Currency;
import com.brokagefirm.broker.enums.OrderSide;
import com.brokagefirm.broker.enums.OrderStatus;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.exception.enums.GenericExceptionMessages;
import com.brokagefirm.broker.mapper.OrderServiceMapper;
import com.brokagefirm.broker.repository.OrderRepository;
import com.brokagefirm.broker.service.dto.AssetDto;
import com.brokagefirm.broker.service.dto.OrderDto;
import com.brokagefirm.broker.service.dto.OrderProcessingDto;
import com.brokagefirm.broker.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.brokagefirm.broker.service.config.ServiceConfigParams.*;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class OrderServiceImpl implements OrderService {
    private final OrderServiceMapper mapper;
    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final AssetService assetService;

    @Override
    public OrderDto createOrder(OrderCreateRequest orderCreateRequest) throws BrokerGenericException {
        customerService.validateCustomerIdIfItsTheCurrentCustomer(orderCreateRequest.getCustomerId());
        if (!customerService.isCustomerExists(orderCreateRequest.getCustomerId())) {
            throw new BrokerGenericException(GenericExceptionMessages.CUSTOMER_NOT_FOUND.getMessage());
        }
        List<AssetDto> customerAssets = assetService.getCustomerAllAssets(orderCreateRequest.getCustomerId());
        OrderProcessingDto orderProcessingDto = OrderProcessingDto.builder()
                .customerAssets(customerAssets)
                .requiredAssetAmount(orderCreateRequest.getSize().multiply(orderCreateRequest.getPrice(), MC).setScale(SCALE, ROUNDING_MODE))
                .tryAsset(customerAssets.stream().filter(asset -> asset.getAssetName().equals(Currency.TRY.getValue())).findFirst().orElseThrow(() -> new BrokerGenericException(GenericExceptionMessages.TRY_ASSET_NOT_FOUND.getMessage())))
                .assetDto(getOrCreateAssetDto(orderCreateRequest, customerAssets))
                .build();
        validateOrder(orderCreateRequest, orderProcessingDto);
        updateAssetUsableSizes(orderCreateRequest, orderProcessingDto);
        Order order = buildOrder(orderCreateRequest);
        return mapper.toOrderDto(orderRepository.save(order));
    }

    private AssetDto getOrCreateAssetDto(OrderCreateRequest orderCreateRequest, List<AssetDto> customerAssets) throws BrokerGenericException {
        Optional<AssetDto> assetDto = customerAssets.stream().filter(asset -> asset.getAssetName().equals(orderCreateRequest.getAssetName())).findFirst();
        if (assetDto.isPresent()) {
            return assetDto.get();
        }
        if (Objects.equals(orderCreateRequest.getOrderSideValue(), OrderSide.SELL.getValue())) {
            throw new BrokerGenericException(GenericExceptionMessages.ASSET_NOT_FOUND.getMessage());
        }
        return assetService.createAsset(AssetCreateRequest.builder()
                .customerId(orderCreateRequest.getCustomerId())
                .assetName(orderCreateRequest.getAssetName())
                .size(BigDecimal.ZERO)
                .usableSize(BigDecimal.ZERO).build());
    }

    private void validateOrder(OrderCreateRequest orderCreateRequest, OrderProcessingDto orderProcessingDto) throws BrokerGenericException {
        if (Objects.equals(orderCreateRequest.getAssetName(), Currency.TRY.getValue())) {
            throw new BrokerGenericException(GenericExceptionMessages.CANT_CREATE_ORDER_FOR_TRY_ASSET.getMessage());
        }
        if (Objects.equals(orderCreateRequest.getOrderSideValue(), OrderSide.BUY.getValue())) {
            if (orderProcessingDto.getTryAsset().getUsableSize().compareTo(orderProcessingDto.getRequiredAssetAmount()) < 0) {
                throw new BrokerGenericException(GenericExceptionMessages.NOT_ENOUGH_TRY_USABLE_SIZE.getMessage());
            }
        } else if (Objects.equals(orderCreateRequest.getOrderSideValue(), OrderSide.SELL.getValue())) {
            if (orderProcessingDto.getAssetDto().getUsableSize().compareTo(orderCreateRequest.getSize()) < 0) {
                throw new BrokerGenericException(GenericExceptionMessages.NOT_ENOUGH_ASSET_USABLE_SIZE.getMessage());
            }
        }
    }

    private Order buildOrder(OrderCreateRequest orderCreateRequest) {
        Order order = new Order();
        order.setCustomer(BrokerCustomer.builder().id(orderCreateRequest.getCustomerId()).build());
        order.setAssetName(orderCreateRequest.getAssetName());
        order.setSide(OrderSide.valueOf(orderCreateRequest.getOrderSideValue()));
        order.setSize(orderCreateRequest.getSize());
        order.setPrice(orderCreateRequest.getPrice());
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    private void updateAssetUsableSizes(OrderCreateRequest orderCreateRequest, OrderProcessingDto orderProcessingDto) throws BrokerGenericException {
        if (Objects.equals(orderCreateRequest.getOrderSideValue(), OrderSide.BUY.getValue())) {
            orderProcessingDto.getTryAsset().setUsableSize(orderProcessingDto.getTryAsset().getUsableSize().subtract(orderProcessingDto.getRequiredAssetAmount(), MC).setScale(SCALE, ROUNDING_MODE));
            assetService.updateAsset(AssetUpdateRequest.builder()
                    .id(orderProcessingDto.getTryAsset().getId())
                    .size(orderProcessingDto.getTryAsset().getSize())
                    .usableSize(orderProcessingDto.getTryAsset().getUsableSize())
                    .build());
        } else if (Objects.equals(orderCreateRequest.getOrderSideValue(), OrderSide.SELL.getValue())) {
            orderProcessingDto.getAssetDto().setUsableSize(orderProcessingDto.getAssetDto().getUsableSize().subtract(orderCreateRequest.getSize(), MC).setScale(SCALE, ROUNDING_MODE));
            assetService.updateAsset(AssetUpdateRequest.builder()
                    .id(orderProcessingDto.getAssetDto().getId())
                    .size(orderProcessingDto.getAssetDto().getSize())
                    .usableSize(orderProcessingDto.getAssetDto().getUsableSize())
                    .build());
        }
    }

    private void updateAssetUsableSizes(Order canceledOrder) throws BrokerGenericException {
        List<AssetDto> customerAssets = assetService.getCustomerAllAssets(canceledOrder.getCustomer().getId());
        if (Objects.equals(canceledOrder.getSide(), OrderSide.BUY)) {
            AssetDto tryAsset = customerAssets.stream().filter(asset -> asset.getAssetName().equals(Currency.TRY.getValue())).findFirst().orElseThrow(() -> new BrokerGenericException(GenericExceptionMessages.TRY_ASSET_NOT_FOUND.getMessage()));
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(canceledOrder.getSize().multiply(canceledOrder.getPrice(), MC), MC).setScale(SCALE, ROUNDING_MODE));
            assetService.updateAsset(AssetUpdateRequest.builder()
                    .id(tryAsset.getId())
                    .size(tryAsset.getSize())
                    .usableSize(tryAsset.getUsableSize())
                    .build());
        } else if (Objects.equals(canceledOrder.getSide(), OrderSide.SELL)) {
            AssetDto assetDto = customerAssets.stream().filter(asset -> asset.getAssetName().equals(canceledOrder.getAssetName())).findFirst().orElseThrow(() -> new BrokerGenericException(GenericExceptionMessages.ASSET_NOT_FOUND.getMessage()));
            assetDto.setUsableSize(assetDto.getUsableSize().add(canceledOrder.getSize(), MC).setScale(SCALE, ROUNDING_MODE));
            assetService.updateAsset(AssetUpdateRequest.builder()
                    .id(assetDto.getId())
                    .size(assetDto.getSize())
                    .usableSize(assetDto.getUsableSize())
                    .build());
        }
    }

    @Override
    public Page<OrderResponse> getCustomerOrders(Long customerId, GetCustomerOrdersRequest request, int pageNo) throws BrokerGenericException {
        customerService.validateCustomerIdIfItsTheCurrentCustomer(customerId);
        Specification<Order> spec = OrderSpecification.search(request, customerId);
        Page<Order> orders = orderRepository.findAll(spec, PageRequest.of(pageNo, DEFAULT_PAGE_SIZE,
                Sort.by("status").descending().and(Sort.by("createdDate").descending())));
        return orders.map(mapper::toOrderResponse);
    }

    @Override
    public OrderDto cancelOrder(Long orderId) throws BrokerGenericException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BrokerGenericException(GenericExceptionMessages.ORDER_NOT_FOUND.getMessage()));
        customerService.validateCustomerIdIfItsTheCurrentCustomer(order.getCustomer().getId());
        if (!Objects.equals(order.getStatus(), OrderStatus.PENDING)) {
            throw new BrokerGenericException(GenericExceptionMessages.ORDER_NOT_PENDING.getMessage());
        }
        updateAssetUsableSizes(order);
        order.setStatus(OrderStatus.CANCELLED);
        return mapper.toOrderDto(orderRepository.save(order));
    }

    @Override
    public OrderDto matchOrder(Long orderId) throws BrokerGenericException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BrokerGenericException(GenericExceptionMessages.ORDER_NOT_FOUND.getMessage()));
        if (!Objects.equals(order.getStatus(), OrderStatus.PENDING)) {
            throw new BrokerGenericException(GenericExceptionMessages.ORDER_NOT_PENDING.getMessage());
        }
        List<AssetDto> customerAssets = assetService.getCustomerAllAssets(order.getCustomer().getId());
        AssetDto tryAsset = customerAssets.stream().filter(asset -> asset.getAssetName().equals(Currency.TRY.getValue())).findFirst().orElseThrow(() -> new BrokerGenericException(GenericExceptionMessages.TRY_ASSET_NOT_FOUND.getMessage()));
        AssetDto assetDto = customerAssets.stream().filter(asset -> asset.getAssetName().equals(order.getAssetName())).findFirst().orElseThrow(() -> new BrokerGenericException(GenericExceptionMessages.ASSET_NOT_FOUND.getMessage()));
        BigDecimal orderAmount = order.getSize().multiply(order.getPrice(), MC).setScale(SCALE, ROUNDING_MODE);
        if (Objects.equals(order.getSide(), OrderSide.BUY)) {
            assetDto.setUsableSize(assetDto.getUsableSize().add(order.getSize(), MC).setScale(SCALE, ROUNDING_MODE));
            assetDto.setSize(assetDto.getSize().add(order.getSize(), MC).setScale(SCALE, ROUNDING_MODE));
            tryAsset.setSize(tryAsset.getSize().subtract(orderAmount, MC).setScale(SCALE, ROUNDING_MODE));
        } else if (Objects.equals(order.getSide(), OrderSide.SELL)) {
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(orderAmount, MC).setScale(SCALE, ROUNDING_MODE));
            tryAsset.setSize(tryAsset.getSize().add(order.getSize().multiply(order.getPrice(), MC), MC).setScale(SCALE, ROUNDING_MODE));
            assetDto.setSize(assetDto.getSize().subtract(order.getSize(), MC).setScale(SCALE, ROUNDING_MODE));
        }
        assetService.updateAsset(AssetUpdateRequest.builder()
                .id(tryAsset.getId())
                .size(tryAsset.getSize())
                .usableSize(tryAsset.getUsableSize())
                .build());
        assetService.updateAsset(AssetUpdateRequest.builder()
                .id(assetDto.getId())
                .size(assetDto.getSize())
                .usableSize(assetDto.getUsableSize())
                .build());
        order.setStatus(OrderStatus.MATCHED);
        return mapper.toOrderDto(orderRepository.save(order));
    }
}
