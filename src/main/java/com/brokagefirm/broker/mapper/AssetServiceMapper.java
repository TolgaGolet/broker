package com.brokagefirm.broker.mapper;

import com.brokagefirm.broker.api.response.AssetResponse;
import com.brokagefirm.broker.entity.Asset;
import com.brokagefirm.broker.service.dto.AssetDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(componentModel = "spring", unmappedTargetPolicy = IGNORE)
public interface AssetServiceMapper {
    @Mapping(source = "customer.id", target = "customerId")
    AssetResponse toAssetResponse(Asset asset);

    @Mapping(source = "customer.id", target = "customerId")
    AssetResponse toAssetResponse(AssetDto asset);

    AssetDto toAssetDto(Asset asset);
}
