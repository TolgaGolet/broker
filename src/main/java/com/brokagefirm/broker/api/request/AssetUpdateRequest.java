package com.brokagefirm.broker.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AssetUpdateRequest {
    @NotNull
    private Long id;
    @NotNull
    @Digits(integer = 16, fraction = 2, message = "Invalid 'size' format")
    @DecimalMin(value = "0.01", message = "Size must be greater than 0.00")
    private BigDecimal size;
    @NotNull
    @Digits(integer = 16, fraction = 2, message = "Invalid 'usableSize' format")
    @DecimalMin(value = "0.01", message = "Usable size must be greater than 0.00")
    private BigDecimal usableSize;
}
