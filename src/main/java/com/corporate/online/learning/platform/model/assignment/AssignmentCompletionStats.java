package com.corporate.online.learning.platform.model.assignment;

import com.corporate.online.learning.platform.model.account.AccountDetails;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assignment_completion_stats")
public class AssignmentCompletionStats {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "completed", nullable = false)
    private Boolean completionStatus;
    private String comment;
    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private AccountDetails accountDetails;
}
