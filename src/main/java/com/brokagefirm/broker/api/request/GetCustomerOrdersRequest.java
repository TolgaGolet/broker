package com.brokagefirm.broker.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GetCustomerOrdersRequest {
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Only alphanumeric characters are allowed")
    private String assetName;
    @Size(min = 1, max = 15)
    private String orderSideValue;
    @Digits(integer = 16, fraction = 2, message = "Invalid size format")
    @DecimalMin(value = "0.01", message = "Size must be greater than 0.00")
    private BigDecimal size;
    @Digits(integer = 16, fraction = 2, message = "Invalid price format")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0.00")
    private BigDecimal price;
    @Size(min = 1, max = 15)
    private String orderStatusValue;
    private int pageNo = 0;
}
