package com.corporate.online.learning.platform.model.account;

import com.corporate.online.learning.platform.model.assignment.AssignmentCompletionStats;
import com.corporate.online.learning.platform.model.course.Course;
import com.corporate.online.learning.platform.model.course.CourseCompletionStats;
import com.corporate.online.learning.platform.model.path.Path;
import com.corporate.online.learning.platform.model.path.PathCompletionStats;
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
@Table(name = "account_details")
public class AccountDetails {

    @Id
    @Column(name = "account_id")
    private Long id;
    @Column(nullable = false)
    private String name;
    private String department;
    private String position;
    private String seniority;
    @MapsId
    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;
    @ManyToMany
    @JoinTable(
            name = "taught_courses",
            joinColumns = @JoinColumn(name = "account_details_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    private List<Course> taughtCourses;
    @OneToMany(mappedBy = "trainerDetails", orphanRemoval = true)
    private List<Path> createdPaths;
    @OneToMany(mappedBy = "accountDetails", orphanRemoval = true)
    private List<CourseCompletionStats> courseCompletionStats;
    @OneToMany(mappedBy = "accountDetails", orphanRemoval = true)
    private List<AssignmentCompletionStats> assignmentCompletionStats;
    @OneToMany(mappedBy = "accountDetails", orphanRemoval = true)
    private List<PathCompletionStats> pathCompletionStats;
}
