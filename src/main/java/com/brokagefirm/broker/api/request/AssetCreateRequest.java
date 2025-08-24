package com.brokagefirm.broker.api.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AssetCreateRequest {
    @NotNull
    private Long customerId;
    @NotNull
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Only alphanumeric characters are allowed")
    private String assetName;
    @NotNull
    @Digits(integer = 16, fraction = 2, message = "Invalid 'size' format")
    @DecimalMin(value = "0.01", message = "Size must be greater than 0.00")
    private BigDecimal size;
    @NotNull
    @Digits(integer = 16, fraction = 2, message = "Invalid 'usableSize' format")
    @DecimalMin(value = "0.01", message = "Usable size must be greater than 0.00")
    private BigDecimal usableSize;
}
