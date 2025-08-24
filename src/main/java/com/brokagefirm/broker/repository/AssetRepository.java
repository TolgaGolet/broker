package com.brokagefirm.broker.repository;

import com.brokagefirm.broker.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {
    Optional<Asset> findByCustomerIdAndAssetName(Long customerId, String assetName);

    List<Asset> findByCustomerId(Long customerId);
}
