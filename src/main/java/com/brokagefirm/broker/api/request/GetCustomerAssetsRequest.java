package com.brokagefirm.broker.api.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GetCustomerAssetsRequest {
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Only alphanumeric characters are allowed")
    private String assetName;
    @Digits(integer = 16, fraction = 2, message = "Invalid size format")
    private BigDecimal size;
    @Digits(integer = 16, fraction = 2, message = "Invalid size format")
    private BigDecimal usableSize;
    private int pageNo = 0;
}
