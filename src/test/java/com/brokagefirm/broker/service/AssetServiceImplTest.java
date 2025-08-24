package com.brokagefirm.broker.service;

import com.brokagefirm.broker.api.request.AssetCreateRequest;
import com.brokagefirm.broker.api.request.AssetUpdateRequest;
import com.brokagefirm.broker.api.request.GetCustomerAssetsRequest;
import com.brokagefirm.broker.api.response.AssetResponse;
import com.brokagefirm.broker.entity.Asset;
import com.brokagefirm.broker.entity.BrokerCustomer;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.mapper.AssetServiceMapper;
import com.brokagefirm.broker.repository.AssetRepository;
import com.brokagefirm.broker.service.dto.AssetDto;
import org.junit.jupiter.api.BeforeEach;
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
class AssetServiceImplTest {
    @Mock
    private AssetServiceMapper mapper;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private CustomerService customerService;

    @InjectMocks
    private AssetServiceImpl assetService;

    private final Long customerId = 1L;

    @BeforeEach
    void setup() {
    }

    @Test
    void createAsset_whenCustomerNotExists_throwsException() throws BrokerGenericException {
        AssetCreateRequest req = AssetCreateRequest.builder()
                .customerId(customerId)
                .assetName("USD")
                .size(new BigDecimal("10.00"))
                .usableSize(new BigDecimal("8.00"))
                .build();
        when(customerService.isCustomerExists(customerId)).thenReturn(false);

        BrokerGenericException ex = assertThrows(BrokerGenericException.class, () -> assetService.createAsset(req));
        assertTrue(ex.getMessage().toLowerCase().contains("customer"));
        verify(assetRepository, never()).saveAndFlush(any());
    }

    @Test
    void createAsset_whenExistingAsset_updatesSizesAndSaves() throws Exception {
        AssetCreateRequest req = AssetCreateRequest.builder()
                .customerId(customerId)
                .assetName("USD")
                .size(new BigDecimal("10.00"))
                .usableSize(new BigDecimal("8.00"))
                .build();
        when(customerService.isCustomerExists(customerId)).thenReturn(true);
        Asset existing = new Asset();
        existing.setId(11L);
        existing.setCustomer(BrokerCustomer.builder().id(customerId).build());
        existing.setAssetName("USD");
        existing.setSize(new BigDecimal("5.00"));
        existing.setUsableSize(new BigDecimal("2.00"));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "USD")).thenReturn(Optional.of(existing));
        when(assetRepository.saveAndFlush(any(Asset.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toAssetDto(any(Asset.class))).thenAnswer(inv -> {
            Asset a = inv.getArgument(0);
            AssetDto dto = new AssetDto();
            dto.setId(a.getId());
            dto.setCustomer(a.getCustomer());
            dto.setAssetName(a.getAssetName());
            dto.setSize(a.getSize());
            dto.setUsableSize(a.getUsableSize());
            return dto;
        });

        AssetDto result = assetService.createAsset(req);

        assertEquals(new BigDecimal("15.00"), result.getSize());
        assertEquals(new BigDecimal("10.00"), result.getUsableSize());

        ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).saveAndFlush(captor.capture());
        Asset saved = captor.getValue();
        assertEquals(new BigDecimal("15.00"), saved.getSize());
        assertEquals(new BigDecimal("10.00"), saved.getUsableSize());
    }

    @Test
    void createAsset_whenNewAsset_scalesAndSaves() throws Exception {
        AssetCreateRequest req = AssetCreateRequest.builder()
                .customerId(customerId)
                .assetName("EUR")
                .size(new BigDecimal("3"))
                .usableSize(new BigDecimal("1"))
                .build();
        when(customerService.isCustomerExists(customerId)).thenReturn(true);
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "EUR")).thenReturn(Optional.empty());
        when(assetRepository.saveAndFlush(any(Asset.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toAssetDto(any(Asset.class))).thenAnswer(inv -> {
            Asset a = inv.getArgument(0);
            AssetDto dto = new AssetDto();
            dto.setCustomer(a.getCustomer());
            dto.setAssetName(a.getAssetName());
            dto.setSize(a.getSize());
            dto.setUsableSize(a.getUsableSize());
            return dto;
        });

        AssetDto dto = assetService.createAsset(req);
        assertEquals(new BigDecimal("3.00"), dto.getSize());
        assertEquals(new BigDecimal("1.00"), dto.getUsableSize());
        assertEquals("EUR", dto.getAssetName());
        assertEquals(customerId, dto.getCustomer().getId());
    }

    @Test
    void updateAsset_whenFound_updatesAndReturnsDto() throws Exception {
        Asset asset = new Asset();
        asset.setId(9L);
        asset.setSize(new BigDecimal("5.00"));
        asset.setUsableSize(new BigDecimal("2.00"));
        when(assetRepository.findById(9L)).thenReturn(Optional.of(asset));
        when(assetRepository.saveAndFlush(any(Asset.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toAssetDto(any(Asset.class))).thenReturn(new AssetDto());

        AssetUpdateRequest req = AssetUpdateRequest.builder()
                .id(9L)
                .size(new BigDecimal("7.77"))
                .usableSize(new BigDecimal("6.66"))
                .build();
        AssetDto dto = assetService.updateAsset(req);
        assertNotNull(dto);
        assertEquals(new BigDecimal("7.77"), asset.getSize());
        assertEquals(new BigDecimal("6.66"), asset.getUsableSize());
    }

    @Test
    void updateAsset_whenNotFound_throwsException() {
        when(assetRepository.findById(99L)).thenReturn(Optional.empty());
        AssetUpdateRequest req = AssetUpdateRequest.builder()
                .id(99L)
                .size(new BigDecimal("1.00"))
                .usableSize(new BigDecimal("1.00"))
                .build();
        assertThrows(BrokerGenericException.class, () -> assetService.updateAsset(req));
    }

    @Test
    void getCustomerAssets_validatesAndReturnsMappedPage() throws Exception {
        doNothing().when(customerService).validateCustomerIdIfItsTheCurrentCustomer(customerId);
        Asset a1 = new Asset();
        a1.setId(1L);
        Asset a2 = new Asset();
        a2.setId(2L);
        Page<Asset> page = new PageImpl<>(List.of(a1, a2));
        when(assetRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
        when(mapper.toAssetResponse(a1)).thenReturn(new AssetResponse(1L, customerId, "USD", new BigDecimal("1.00"), new BigDecimal("1.00")));
        when(mapper.toAssetResponse(a2)).thenReturn(new AssetResponse(2L, customerId, "EUR", new BigDecimal("2.00"), new BigDecimal("2.00")));

        Page<AssetResponse> result = assetService.getCustomerAssets(customerId, GetCustomerAssetsRequest.builder().build(), 0);

        assertEquals(2, result.getContent().size());
        verify(customerService).validateCustomerIdIfItsTheCurrentCustomer(customerId);
        verify(assetRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void getCustomerAllAssets_mapsAll() throws Exception {
        Asset a1 = new Asset();
        a1.setId(1L);
        Asset a2 = new Asset();
        a2.setId(2L);
        when(assetRepository.findByCustomerId(customerId)).thenReturn(List.of(a1, a2));
        AssetDto d1 = new AssetDto();
        d1.setId(1L);
        AssetDto d2 = new AssetDto();
        d2.setId(2L);
        when(mapper.toAssetDto(a1)).thenReturn(d1);
        when(mapper.toAssetDto(a2)).thenReturn(d2);

        List<AssetDto> list = assetService.getCustomerAllAssets(customerId);
        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals(2L, list.get(1).getId());
    }

    @Test
    void deleteAsset_whenFound_validatesAndDeletes() throws Exception {
        Asset asset = new Asset();
        asset.setId(5L);
        asset.setCustomer(BrokerCustomer.builder().id(customerId).build());
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        doNothing().when(customerService).validateCustomerIdIfItsTheCurrentCustomer(customerId);

        assetService.deleteAsset(5L);

        verify(customerService).validateCustomerIdIfItsTheCurrentCustomer(customerId);
        verify(assetRepository).delete(asset);
    }

    @Test
    void deleteAsset_whenNotFound_throwsException() {
        when(assetRepository.findById(77L)).thenReturn(Optional.empty());
        assertThrows(BrokerGenericException.class, () -> assetService.deleteAsset(77L));
    }
}


