package com.brokagefirm.broker.entity;

import com.brokagefirm.broker.entity.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(columnList = "customer_id")
})
public class Asset extends Auditable {
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
    @Column(precision = 18, scale = 2)
    @Digits(integer = 16, fraction = 2, message = "Invalid 'size' format")
    private BigDecimal size;
    @NotNull
    @Column(precision = 18, scale = 2)
    @Digits(integer = 16, fraction = 2, message = "Invalid 'usableSize' format")
    private BigDecimal usableSize;
}
