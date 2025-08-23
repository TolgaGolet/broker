package com.brokagefirm.broker.entity;

import com.brokagefirm.broker.entity.audit.Auditable;
import com.brokagefirm.broker.enums.OrderSide;
import com.brokagefirm.broker.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
    private BigDecimal size;
    @NotNull
    private BigDecimal price;
    @NotNull
    @Column(length = 15)
    private OrderStatus status;
}
