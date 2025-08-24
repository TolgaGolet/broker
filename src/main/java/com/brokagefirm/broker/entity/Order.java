package com.brokagefirm.broker.entity;

import com.brokagefirm.broker.entity.audit.Auditable;
import com.brokagefirm.broker.enums.OrderSide;
import com.brokagefirm.broker.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders", indexes = {
        @Index(columnList = "customer_id"),
        @Index(columnList = "status")
})
public class Order extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, updatable = false)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BrokerCustomer customer;
    @NotNull
    @Size(min = 3, max = 50)
    @Column(length = 50)
    private String assetName;
    @NotNull
    @Column(length = 15)
    private OrderSide side;
    @NotNull
    @Column(precision = 18, scale = 2)
    @Digits(integer = 16, fraction = 2, message = "Invalid 'size' format")
    @DecimalMin(value = "0.01", message = "Size must be greater than 0.00")
    private BigDecimal size;
    @NotNull
    @Column(precision = 18, scale = 2)
    @Digits(integer = 16, fraction = 2, message = "Invalid 'price' format")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0.00")
    private BigDecimal price;
    @NotNull
    @Column(length = 15)
    private OrderStatus status;
}
