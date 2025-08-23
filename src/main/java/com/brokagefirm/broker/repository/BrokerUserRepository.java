package com.brokagefirm.broker.repository;

import com.brokagefirm.broker.entity.BrokerUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrokerUserRepository extends JpaRepository<BrokerUser, Long> {
    Optional<BrokerUser> findByUsername(String username);
}
