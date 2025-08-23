package com.brokagefirm.broker.repository;

import com.brokagefirm.broker.entity.CustomerToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerTokenRepository extends JpaRepository<CustomerToken, Long> {
    @Query(value = """
            select ct from CustomerToken ct inner join BrokerCustomer bc\s
            on ct.customer.id = bc.id\s
            where bc.id = :id and (ct.expired = false or ct.revoked = false)\s
            """)
    List<CustomerToken> findAllValidCustomerTokensByCustomer(Long id);

    @Modifying
    @Query(value = """
            DELETE FROM customer_token ct
              WHERE ct.customer_id = :customerId
              AND (SELECT COUNT(*) FROM public.customer_token ct2 WHERE ct2.customer_id = :customerId) > :count
              AND ct.id IN (
                  SELECT ct2.id
                  FROM public.customer_token ct2
                  WHERE ct2.customer_id = :customerId
                  ORDER BY ct2.created_date
                  LIMIT (SELECT COUNT(*) - :count FROM public.customer_token ct3 WHERE ct3.customer_id = :customerId)
              );
            """, nativeQuery = true)
    void deleteOlderTokensOfCustomerByCount(Long customerId, Integer count);

    Optional<CustomerToken> findByToken(String token);
}
