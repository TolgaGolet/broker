package com.brokagefirm.broker.repository;

import com.brokagefirm.broker.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    @Query(value = """
            select ut from UserToken ut inner join BrokerUser bu\s
            on ut.user.id = bu.id\s
            where bu.id = :id and (ut.expired = false or ut.revoked = false)\s
            """)
    List<UserToken> findAllValidUserTokensByUser(Long id);

    @Modifying
    @Query(value = """
            DELETE FROM user_token ut
              WHERE ut.user_id = :userId
              AND (SELECT COUNT(*) FROM public.user_token ut2 WHERE ut2.user_id = :userId) > :count
              AND ut.id IN (
                  SELECT ut2.id
                  FROM public.user_token ut2
                  WHERE ut2.user_id = :userId
                  ORDER BY ut2.created_date
                  LIMIT (SELECT COUNT(*) - :count FROM public.user_token ut3 WHERE ut3.user_id = :userId)
              );
            """, nativeQuery = true)
    void deleteOlderTokensOfUserByCount(Long userId, Integer count);

    Optional<UserToken> findByToken(String token);
}
