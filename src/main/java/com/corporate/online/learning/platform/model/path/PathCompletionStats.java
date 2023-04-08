package com.corporate.online.learning.platform.model.path;

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
@Table(name = "path_completion_stats")
public class PathCompletionStats {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "completed", nullable = false)
    private Boolean completionStatus;
    @Column(name = "completed_courses", nullable = false)
    private Integer completedCourses;
    @ManyToOne
    @JoinColumn(name = "path_id", nullable = false)
    private Path path;
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private AccountDetails accountDetails;
}
