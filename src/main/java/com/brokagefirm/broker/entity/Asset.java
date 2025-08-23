package com.brokagefirm.broker.entity;

import com.brokagefirm.broker.entity.audit.Auditable;
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
    private BigDecimal size;
    @NotNull
    private BigDecimal usableSize;
}
