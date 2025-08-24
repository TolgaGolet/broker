package com.brokagefirm.broker.service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderProcessingDto {
    private List<AssetDto> customerAssets;
    BigDecimal requiredAssetAmount;
    AssetDto tryAsset;
    AssetDto assetDto;
}
