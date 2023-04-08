package com.corporate.online.learning.platform.model.course;

import com.corporate.online.learning.platform.model.account.AccountDetails;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "course_completion_stats")
public class CourseCompletionStats {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "completed", nullable = false)
    private Boolean completionStatus;
    @Column(name = "completed_assignments", nullable = false)
    private Integer completedAssignments;
    @Column(name = "date", nullable = false)
    private Timestamp enrollmentDate;
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private AccountDetails accountDetails;
}
