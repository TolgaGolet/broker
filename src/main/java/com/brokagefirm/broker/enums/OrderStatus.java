package com.brokagefirm.broker.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OrderStatus {
    PENDING("PENDING", "Pending"),
    MATCHED("MATCHED", "Matched"),
    CANCELLED("CANCELLED", "Cancelled");

    private final String value;
    private final String label;

    @JsonCreator
    public static OrderStatus of(@JsonProperty("value") String value) {
        return Arrays.stream(OrderStatus.values()).filter(item -> Objects.equals(item.getValue(), value)).findFirst().orElseThrow(() -> new IllegalArgumentException("IllegalArgumentException with value: " + value));
    }
}
