package com.corporate.online.learning.platform.repository.assignment;

import com.corporate.online.learning.platform.model.account.AccountDetails;
import com.corporate.online.learning.platform.model.assignment.AssignmentCompletionStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentCompletionStatsRepository extends JpaRepository<AssignmentCompletionStats, Long> {

    List<AssignmentCompletionStats> findByAccountDetails(AccountDetails accountDetails, Pageable pageable);
}
