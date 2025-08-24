package com.brokagefirm.broker.api.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GetCustomerOrdersRequest {
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Only alphanumeric characters are allowed")
    private String assetName;
    @Size(min = 1, max = 15)
    private String orderSideValue;
    @Digits(integer = 16, fraction = 2, message = "Invalid 'size' format")
    @DecimalMin(value = "0.01", message = "Size must be greater than 0.00")
    private BigDecimal size;
    @Digits(integer = 16, fraction = 2, message = "Invalid 'price' format")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0.00")
    private BigDecimal price;
    @Size(min = 1, max = 15)
    private String orderStatusValue;
    @Schema(example = "2025-08-20T00:01:06.984")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private LocalDateTime startDate;
    @Schema(example = "2025-08-20T00:01:06.984")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private LocalDateTime endDate;
}
