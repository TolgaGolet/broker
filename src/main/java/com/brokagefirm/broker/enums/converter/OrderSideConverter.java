package com.brokagefirm.broker.enums.converter;

import com.brokagefirm.broker.enums.OrderSide;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Objects;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class OrderSideConverter implements AttributeConverter<OrderSide, String> {
    @Override
    public String convertToDatabaseColumn(OrderSide attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public OrderSide convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Stream.of(OrderSide.values()).filter(item -> Objects.equals(item.getValue(), dbData)).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
