package com.corporate.online.learning.platform.repository.account;

import com.corporate.online.learning.platform.model.account.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("""
            select t from Token t inner join Account a on t.account.id = a.id
            where a.id = :accountId and t.expired = false
            """)
    List<Token> findAllValidTokensByAccountId(Long accountId);

    Optional<Token> findByToken(String token);
}
