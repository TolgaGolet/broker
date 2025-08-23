package com.brokagefirm.broker.specification;

import com.brokagefirm.broker.api.request.GetCustomerAssetsRequest;
import com.brokagefirm.broker.entity.Asset;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AssetSpecification {

    public static Specification<Asset> search(GetCustomerAssetsRequest request, Long customerId) {
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
            if (request.getSize() != null) {
                predicates.add(criteriaBuilder.equal(root.get("size"), request.getSize()));
            }
            if (request.getUsableSize() != null) {
                predicates.add(criteriaBuilder.equal(root.get("usableSize"), request.getUsableSize()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
