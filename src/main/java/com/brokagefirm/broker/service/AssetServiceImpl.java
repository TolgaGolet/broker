package com.brokagefirm.broker.service;

import com.brokagefirm.broker.api.request.GetCustomerAssetsRequest;
import com.brokagefirm.broker.api.response.AssetResponse;
import com.brokagefirm.broker.entity.Asset;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.mapper.AssetServiceMapper;
import com.brokagefirm.broker.repository.AssetRepository;
import com.brokagefirm.broker.specification.AssetSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.brokagefirm.broker.security.SecurityParams.DEFAULT_PAGE_SIZE;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetServiceImpl implements AssetService {
    private final AssetServiceMapper mapper;
    private final AssetRepository assetRepository;
    private final CustomerService customerService;

    @Override
    public Page<AssetResponse> getCustomerAssets(Long customerId, GetCustomerAssetsRequest request) throws BrokerGenericException {
        customerService.validateCustomerIdIfItsTheCurrentCustomer(customerId);
        Specification<Asset> spec = AssetSpecification.search(request, customerId);
        Page<Asset> assets = assetRepository.findAll(spec, PageRequest.of(request.getPageNo(), DEFAULT_PAGE_SIZE, Sort.by("assetName").descending()));
        return assets.map(mapper::toAssetResponse);
    }
}
