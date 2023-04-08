package com.corporate.online.learning.platform.repository.account;

import com.corporate.online.learning.platform.model.account.AccountDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountDetailsRepository extends JpaRepository<AccountDetails, Long> {
}
