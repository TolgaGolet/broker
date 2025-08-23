package com.brokagefirm.broker.service;

import com.brokagefirm.broker.api.request.GetCustomerAssetsRequest;
import com.brokagefirm.broker.api.response.AssetResponse;
import com.brokagefirm.broker.exception.BrokerGenericException;
import org.springframework.data.domain.Page;

public interface AssetService {
    Page<AssetResponse> getCustomerAssets(Long customerId, GetCustomerAssetsRequest request) throws BrokerGenericException;
}
