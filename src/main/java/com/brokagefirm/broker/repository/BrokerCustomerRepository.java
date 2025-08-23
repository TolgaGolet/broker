package com.brokagefirm.broker.repository;

import com.brokagefirm.broker.entity.BrokerCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrokerCustomerRepository extends JpaRepository<BrokerCustomer, Long> {
    Optional<BrokerCustomer> findByUsername(String username);
}
