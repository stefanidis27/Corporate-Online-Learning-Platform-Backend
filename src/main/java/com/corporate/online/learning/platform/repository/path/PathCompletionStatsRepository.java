package com.corporate.online.learning.platform.repository.path;

import com.corporate.online.learning.platform.model.account.AccountDetails;
import com.corporate.online.learning.platform.model.path.PathCompletionStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PathCompletionStatsRepository extends JpaRepository<PathCompletionStats, Long> {

    List<PathCompletionStats> findByAccountDetails(AccountDetails accountDetails, Pageable pageable);
}
