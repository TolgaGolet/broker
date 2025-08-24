package com.brokagefirm.broker.service.dto;

import com.brokagefirm.broker.entity.BrokerCustomer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetDto {
    private Long id;
    private BrokerCustomer customer;
    private String assetName;
    private BigDecimal size;
    private BigDecimal usableSize;
}
