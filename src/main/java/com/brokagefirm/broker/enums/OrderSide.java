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
public enum OrderSide {
    BUY("BUY", "Buy"),
    SELL("SELL", "Sell");

    private final String value;
    private final String label;

    @JsonCreator
    public static OrderSide of(@JsonProperty("value") String value) {
        return Arrays.stream(OrderSide.values()).filter(item -> Objects.equals(item.getValue(), value)).findFirst().orElseThrow(() -> new IllegalArgumentException("IllegalArgumentException with value: " + value));
    }
}
