package com.brokagefirm.broker.service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetDto {
    private Long customerId;
    private String assetName;
    private BigDecimal size;
    private BigDecimal usableSize;
}
