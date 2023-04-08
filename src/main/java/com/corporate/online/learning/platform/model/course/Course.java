package com.corporate.online.learning.platform.model.course;

import com.corporate.online.learning.platform.model.account.AccountDetails;
import com.corporate.online.learning.platform.model.assignment.Assignment;
import com.corporate.online.learning.platform.model.path.Path;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String category;
    @Column(name = "max_enrollments", nullable = false)
    private Long maxEnrollments;
    @Column(name = "self_enrollment", nullable = false)
    private Boolean selfEnrollment;
    private String description;
    @Column(name = "no_assignments", nullable = false)
    private Integer numberOfAssignments;
    @Column(nullable = false)
    private Long completions;
    @Column(name = "un_enrollments", nullable = false)
    private Long unEnrollments;
    @Column(name = "current_enrollments", nullable = false)
    private Long currentEnrollments;
    @ManyToMany(mappedBy = "taughtCourses")
    @Column(nullable = false)
    private List<AccountDetails> trainersDetails;
    @OneToMany(mappedBy = "course", orphanRemoval = true)
    private List<Assignment> assignments;
    @OneToMany(mappedBy = "course", orphanRemoval = true)
    private List<CourseCompletionStats> courseCompletionStats;
    @ManyToMany(mappedBy = "courses")
    @Column(nullable = false)
    private List<Path> paths;
}
