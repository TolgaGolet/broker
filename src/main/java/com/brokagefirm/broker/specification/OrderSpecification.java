package com.brokagefirm.broker.specification;

import com.brokagefirm.broker.api.request.GetCustomerOrdersRequest;
import com.brokagefirm.broker.entity.Order;
import com.brokagefirm.broker.enums.OrderSide;
import com.brokagefirm.broker.enums.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    public static Specification<Order> search(GetCustomerOrdersRequest request, Long customerId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (customerId != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer").get("id"), customerId));
            }
            if (request.getAssetName() != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("assetName")),
                        "%" + request.getAssetName().trim().toLowerCase() + "%"
                ));
            }
            if (request.getOrderSideValue() != null) {
                predicates.add(criteriaBuilder.equal(root.get("side"), OrderSide.of(request.getOrderSideValue())));
            }
            if (request.getSize() != null) {
                predicates.add(criteriaBuilder.equal(root.get("size"), request.getSize()));
            }
            if (request.getPrice() != null) {
                predicates.add(criteriaBuilder.equal(root.get("price"), request.getPrice()));
            }
            if (request.getOrderStatusValue() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), OrderStatus.of(request.getOrderStatusValue())));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
