package com.brokagefirm.broker.service;

import com.brokagefirm.broker.api.request.AssetCreateRequest;
import com.brokagefirm.broker.api.request.AssetUpdateRequest;
import com.brokagefirm.broker.api.request.GetCustomerAssetsRequest;
import com.brokagefirm.broker.api.response.AssetResponse;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.service.dto.AssetDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AssetService {
    AssetDto createAsset(AssetCreateRequest assetCreateRequest) throws BrokerGenericException;

    AssetDto updateAsset(AssetUpdateRequest assetUpdateRequest) throws BrokerGenericException;

    Page<AssetResponse> getCustomerAssets(Long customerId, GetCustomerAssetsRequest request, int pageNo) throws BrokerGenericException;

    List<AssetDto> getCustomerAllAssets(Long customerId) throws BrokerGenericException;

    void deleteAsset(Long assetId) throws BrokerGenericException;
}
