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
import com.brokagefirm.broker.mapper.OrderServiceMapper;
import com.brokagefirm.broker.repository.OrderRepository;
import com.brokagefirm.broker.service.dto.AssetDto;
import com.brokagefirm.broker.service.dto.OrderDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock
    private OrderServiceMapper mapper;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerService customerService;
    @Mock
    private AssetService assetService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderCreateRequest baseCreateRequest(Long customerId, String assetName, String side, String size, String price) {
        OrderCreateRequest req = new OrderCreateRequest();
        req.setCustomerId(customerId);
        req.setAssetName(assetName);
        req.setOrderSideValue(side);
        req.setSize(new BigDecimal(size));
        req.setPrice(new BigDecimal(price));
        return req;
    }

    private AssetDto asset(String name, String size, String usable) {
        AssetDto dto = new AssetDto();
        dto.setId((long) name.hashCode());
        dto.setCustomer(BrokerCustomer.builder().id(1L).build());
        dto.setAssetName(name);
        dto.setSize(new BigDecimal(size));
        dto.setUsableSize(new BigDecimal(usable));
        return dto;
    }

    @Test
    void createOrder_whenCustomerNotExists_throws() throws Exception {
        OrderCreateRequest req = baseCreateRequest(1L, "USD", OrderSide.BUY.getValue(), "1.00", "10.00");
        doNothing().when(customerService).validateCustomerIdIfItsTheCurrentCustomer(1L);
        when(customerService.isCustomerExists(1L)).thenReturn(false);

        assertThrows(BrokerGenericException.class, () -> orderService.createOrder(req));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_whenAssetIsTRY_throws() throws Exception {
        OrderCreateRequest req = baseCreateRequest(1L, Currency.TRY.getValue(), OrderSide.BUY.getValue(), "1.00", "10.00");
        doNothing().when(customerService).validateCustomerIdIfItsTheCurrentCustomer(1L);
        when(customerService.isCustomerExists(1L)).thenReturn(true);
        when(assetService.getCustomerAllAssets(1L)).thenReturn(List.of(asset(Currency.TRY.getValue(), "100.00", "100.00")));

        BrokerGenericException ex = assertThrows(BrokerGenericException.class, () -> orderService.createOrder(req));
        assertTrue(ex.getMessage().toLowerCase().contains("try"));
    }

    @Test
    void createOrder_buy_whenNotEnoughTry_throws() throws Exception {
        OrderCreateRequest req = baseCreateRequest(1L, "USD", OrderSide.BUY.getValue(), "10.00", "10.00");
        doNothing().when(customerService).validateCustomerIdIfItsTheCurrentCustomer(1L);
        when(customerService.isCustomerExists(1L)).thenReturn(true);
        AssetDto tryAsset = asset(Currency.TRY.getValue(), "5.00", "5.00");
        AssetDto usd = asset("USD", "0.00", "0.00");
        when(assetService.getCustomerAllAssets(1L)).thenReturn(List.of(tryAsset, usd));

        assertThrows(BrokerGenericException.class, () -> orderService.createOrder(req));
    }

    @Test
    void createOrder_sell_whenAssetMissing_throws() throws Exception {
        OrderCreateRequest req = baseCreateRequest(1L, "USD", OrderSide.SELL.getValue(), "1.00", "10.00");
        doNothing().when(customerService).validateCustomerIdIfItsTheCurrentCustomer(1L);
        when(customerService.isCustomerExists(1L)).thenReturn(true);
        AssetDto tryAsset = asset(Currency.TRY.getValue(), "100.00", "100.00");
        when(assetService.getCustomerAllAssets(1L)).thenReturn(List.of(tryAsset));

        assertThrows(BrokerGenericException.class, () -> orderService.createOrder(req));
    }

    @Test
    void createOrder_sell_whenNotEnoughUsable_throws() throws Exception {
        OrderCreateRequest req = baseCreateRequest(1L, "USD", OrderSide.SELL.getValue(), "10.00", "10.00");
        doNothing().when(customerService).validateCustomerIdIfItsTheCurrentCustomer(1L);
        when(customerService.isCustomerExists(1L)).thenReturn(true);
        AssetDto tryAsset = asset(Currency.TRY.getValue(), "100.00", "100.00");
        AssetDto usd = asset("USD", "5.00", "4.00");
        when(assetService.getCustomerAllAssets(1L)).thenReturn(List.of(tryAsset, usd));

        assertThrows(BrokerGenericException.class, () -> orderService.createOrder(req));
    }

    @Test
    void createOrder_buy_happyPath_updatesTryUsable_savesOrder() throws Exception {
        OrderCreateRequest req = baseCreateRequest(1L, "USD", OrderSide.BUY.getValue(), "2.00", "10.00");
        doNothing().when(customerService).validateCustomerIdIfItsTheCurrentCustomer(1L);
        when(customerService.isCustomerExists(1L)).thenReturn(true);
        AssetDto tryAsset = asset(Currency.TRY.getValue(), "100.00", "100.00");
        AssetDto usd = asset("USD", "0.00", "0.00");
        when(assetService.getCustomerAllAssets(1L)).thenReturn(List.of(tryAsset, usd));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toOrderDto(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            OrderDto d = new OrderDto();
            d.setOrderId(o.getId());
            d.setCustomer(o.getCustomer());
            d.setSize(o.getSize());
            d.setPrice(o.getPrice());
            d.setOrderStatus(o.getStatus());
            d.setOrderSide(o.getSide());
            return d;
        });

        OrderDto dto = orderService.createOrder(req);
        assertNotNull(dto);
        // TRY usable should decrease by 20.00
        ArgumentCaptor<AssetUpdateRequest> captor = ArgumentCaptor.forClass(AssetUpdateRequest.class);
        verify(assetService, times(1)).updateAsset(captor.capture());
        AssetUpdateRequest upd = captor.getValue();
        assertEquals(new BigDecimal("80.00"), upd.getUsableSize());
    }

    @Test
    void createOrder_buy_whenAssetNotPresent_createsAsset() throws Exception {
        OrderCreateRequest req = baseCreateRequest(1L, "GBP", OrderSide.BUY.getValue(), "1.00", "10.00");
        doNothing().when(customerService).validateCustomerIdIfItsTheCurrentCustomer(1L);
        when(customerService.isCustomerExists(1L)).thenReturn(true);
        AssetDto tryAsset = asset(Currency.TRY.getValue(), "100.00", "100.00");
        when(assetService.getCustomerAllAssets(1L)).thenReturn(List.of(tryAsset));
        when(assetService.createAsset(any(AssetCreateRequest.class))).thenAnswer(inv -> {
            AssetCreateRequest ar = inv.getArgument(0);
            AssetDto d = new AssetDto();
            d.setId(123L);
            d.setCustomer(BrokerCustomer.builder().id(ar.getCustomerId()).build());
            d.setAssetName(ar.getAssetName());
            d.setSize(ar.getSize());
            d.setUsableSize(ar.getUsableSize());
            return d;
        });
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toOrderDto(any(Order.class))).thenReturn(new OrderDto());

        orderService.createOrder(req);
        verify(assetService).createAsset(any(AssetCreateRequest.class));
    }

    @Test
    void createOrder_sell_happyPath_updatesAssetUsable() throws Exception {
        OrderCreateRequest req = baseCreateRequest(1L, "USD", OrderSide.SELL.getValue(), "2.00", "10.00");
        doNothing().when(customerService).validateCustomerIdIfItsTheCurrentCustomer(1L);
        when(customerService.isCustomerExists(1L)).thenReturn(true);
        AssetDto tryAsset = asset(Currency.TRY.getValue(), "100.00", "100.00");
        AssetDto usd = asset("USD", "10.00", "5.00");
        when(assetService.getCustomerAllAssets(1L)).thenReturn(List.of(tryAsset, usd));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toOrderDto(any(Order.class))).thenReturn(new OrderDto());

        orderService.createOrder(req);

        ArgumentCaptor<AssetUpdateRequest> captor = ArgumentCaptor.forClass(AssetUpdateRequest.class);
        verify(assetService, times(1)).updateAsset(captor.capture());
        AssetUpdateRequest upd = captor.getValue();
        assertEquals(new BigDecimal("3.00"), upd.getUsableSize());
    }

    @Test
    void getCustomerOrders_validatesAndMaps() throws Exception {
        doNothing().when(customerService).validateCustomerIdIfItsTheCurrentCustomer(1L);
        Order o1 = new Order();
        Order o2 = new Order();
        Page<Order> page = new PageImpl<>(List.of(o1, o2));
        when(orderRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
        when(mapper.toOrderResponse(o1)).thenReturn(new OrderResponse());
        when(mapper.toOrderResponse(o2)).thenReturn(new OrderResponse());

        Page<OrderResponse> res = orderService.getCustomerOrders(1L, new GetCustomerOrdersRequest(), 0);
        assertEquals(2, res.getTotalElements());
    }

    @Test
    void cancelOrder_whenNotFound_throws() {
        when(orderRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(BrokerGenericException.class, () -> orderService.cancelOrder(5L));
    }

    @Test
    void cancelOrder_whenNotPending_throws() throws BrokerGenericException {
        Order o = new Order();
        o.setCustomer(BrokerCustomer.builder().id(1L).build());
        o.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));
        doNothing().when(customerService).validateCustomerIdIfItsTheCurrentCustomer(1L);

        assertThrows(BrokerGenericException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    void cancelOrder_happyPath_restoresUsableAndSaves() throws Exception {
        Order o = new Order();
        o.setId(10L);
        o.setCustomer(BrokerCustomer.builder().id(1L).build());
        o.setAssetName("USD");
        o.setSide(OrderSide.BUY);
        o.setSize(new BigDecimal("2.00"));
        o.setPrice(new BigDecimal("10.00"));
        o.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(o));
        doNothing().when(customerService).validateCustomerIdIfItsTheCurrentCustomer(1L);

        AssetDto tryAsset = asset(Currency.TRY.getValue(), "100.00", "80.00");
        AssetDto usd = asset("USD", "1.00", "0.00");
        when(assetService.getCustomerAllAssets(1L)).thenReturn(List.of(tryAsset, usd));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toOrderDto(any(Order.class))).thenReturn(new OrderDto());

        orderService.cancelOrder(10L);

        // Two updates: TRY restored usable; or asset usable restored when SELL
        verify(assetService, times(1)).updateAsset(any(AssetUpdateRequest.class));
        assertEquals(OrderStatus.CANCELLED, o.getStatus());
    }

    @Test
    void matchOrder_whenNotFound_throws() {
        when(orderRepository.findById(7L)).thenReturn(Optional.empty());
        assertThrows(BrokerGenericException.class, () -> orderService.matchOrder(7L));
    }

    @Test
    void matchOrder_whenNotPending_throws() {
        Order o = new Order();
        o.setStatus(OrderStatus.MATCHED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));
        assertThrows(BrokerGenericException.class, () -> orderService.matchOrder(1L));
    }

    @Test
    void matchOrder_buy_happyPath_updatesBothAssetsAndSaves() throws Exception {
        Order o = new Order();
        o.setId(20L);
        o.setCustomer(BrokerCustomer.builder().id(1L).build());
        o.setAssetName("USD");
        o.setSide(OrderSide.BUY);
        o.setSize(new BigDecimal("2.00"));
        o.setPrice(new BigDecimal("10.00"));
        o.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(20L)).thenReturn(Optional.of(o));
        AssetDto tryAsset = asset(Currency.TRY.getValue(), "100.00", "60.00");
        AssetDto usd = asset("USD", "1.00", "1.00");
        when(assetService.getCustomerAllAssets(1L)).thenReturn(List.of(tryAsset, usd));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toOrderDto(any(Order.class))).thenReturn(new OrderDto());

        orderService.matchOrder(20L);
        // Two updates are performed
        verify(assetService, times(2)).updateAsset(any(AssetUpdateRequest.class));
        assertEquals(OrderStatus.MATCHED, o.getStatus());
    }

    @Test
    void matchOrder_sell_happyPath_updatesBothAssetsAndSaves() throws Exception {
        Order o = new Order();
        o.setId(21L);
        o.setCustomer(BrokerCustomer.builder().id(1L).build());
        o.setAssetName("USD");
        o.setSide(OrderSide.SELL);
        o.setSize(new BigDecimal("2.00"));
        o.setPrice(new BigDecimal("10.00"));
        o.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(21L)).thenReturn(Optional.of(o));
        AssetDto tryAsset = asset(Currency.TRY.getValue(), "100.00", "60.00");
        AssetDto usd = asset("USD", "10.00", "3.00");
        when(assetService.getCustomerAllAssets(1L)).thenReturn(List.of(tryAsset, usd));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toOrderDto(any(Order.class))).thenReturn(new OrderDto());

        orderService.matchOrder(21L);
        verify(assetService, times(2)).updateAsset(any(AssetUpdateRequest.class));
        assertEquals(OrderStatus.MATCHED, o.getStatus());
    }
}


