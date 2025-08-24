package com.brokagefirm.broker.service;

import com.brokagefirm.broker.api.request.AssetCreateRequest;
import com.brokagefirm.broker.api.request.AssetUpdateRequest;
import com.brokagefirm.broker.api.request.GetCustomerAssetsRequest;
import com.brokagefirm.broker.api.response.AssetResponse;
import com.brokagefirm.broker.entity.Asset;
import com.brokagefirm.broker.entity.BrokerCustomer;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.exception.enums.GenericExceptionMessages;
import com.brokagefirm.broker.mapper.AssetServiceMapper;
import com.brokagefirm.broker.repository.AssetRepository;
import com.brokagefirm.broker.service.dto.AssetDto;
import com.brokagefirm.broker.specification.AssetSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.brokagefirm.broker.service.config.ServiceConfigParams.*;
import static com.brokagefirm.broker.service.util.BigDecimalUtils.*;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class AssetServiceImpl implements AssetService {
    private final AssetServiceMapper mapper;
    private final AssetRepository assetRepository;
    private final CustomerService customerService;

    @Override
    public AssetDto createAsset(AssetCreateRequest assetCreateRequest) throws BrokerGenericException {
        if (!customerService.isCustomerExists(assetCreateRequest.getCustomerId())) {
            throw new BrokerGenericException(GenericExceptionMessages.CUSTOMER_NOT_FOUND.getMessage());
        }
        Optional<Asset> existingAsset = assetRepository.findByCustomerIdAndAssetName(assetCreateRequest.getCustomerId(), assetCreateRequest.getAssetName());
        Asset asset = existingAsset.orElseGet(Asset::new);
        asset.setCustomer(BrokerCustomer.builder().id(assetCreateRequest.getCustomerId()).build());
        asset.setAssetName(assetCreateRequest.getAssetName());
        asset.setSize(existingAsset.isPresent() ? add(asset.getSize(), assetCreateRequest.getSize()) : scale(assetCreateRequest.getSize()));
        asset.setUsableSize(existingAsset.isPresent() ? add(asset.getUsableSize(), assetCreateRequest.getUsableSize()) : scale(assetCreateRequest.getUsableSize()));

        return mapper.toAssetDto(assetRepository.saveAndFlush(asset));
    }

    @Override
    public AssetDto updateAsset(AssetUpdateRequest assetUpdateRequest) throws BrokerGenericException {
        Asset asset = assetRepository.findById(assetUpdateRequest.getId()).orElseThrow(() -> new BrokerGenericException(GenericExceptionMessages.ASSET_NOT_FOUND.getMessage()));
        asset.setSize(assetUpdateRequest.getSize());
        asset.setUsableSize(assetUpdateRequest.getUsableSize());
        return mapper.toAssetDto(assetRepository.saveAndFlush(asset));
    }

    @Override
    public Page<AssetResponse> getCustomerAssets(Long customerId, GetCustomerAssetsRequest request, int pageNo) throws BrokerGenericException {
        customerService.validateCustomerIdIfItsTheCurrentCustomer(customerId);
        Specification<Asset> spec = AssetSpecification.search(request, customerId);
        Page<Asset> assets = assetRepository.findAll(spec, PageRequest.of(pageNo, DEFAULT_PAGE_SIZE, Sort.by("assetName").descending()));
        return assets.map(mapper::toAssetResponse);
    }

    @Override
    public List<AssetDto> getCustomerAllAssets(Long customerId) throws BrokerGenericException {
        List<Asset> assets = assetRepository.findByCustomerId(customerId);
        return assets.stream().map(mapper::toAssetDto).toList();
    }

    @Override
    public void deleteAsset(Long assetId) throws BrokerGenericException {
        Asset asset = assetRepository.findById(assetId).orElseThrow(() -> new BrokerGenericException(GenericExceptionMessages.ASSET_NOT_FOUND.getMessage()));
        customerService.validateCustomerIdIfItsTheCurrentCustomer(asset.getCustomer().getId());
        assetRepository.delete(asset);
    }
}
