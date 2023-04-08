package com.corporate.online.learning.platform.model.path;

import com.corporate.online.learning.platform.model.account.AccountDetails;
import com.corporate.online.learning.platform.model.course.Course;
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
@Table(name = "path")
public class Path {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String category;
    @Column(name = "courses", nullable = false)
    private Integer numberOfCourses;
    @Column(name = "current_enrollments", nullable = false)
    private Long currentEnrollments;
    @Column(nullable = false)
    private Long completions;
    @OneToMany(mappedBy = "path", orphanRemoval = true)
    private List<PathCompletionStats> pathCompletionStats;
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private AccountDetails trainerDetails;
    @ManyToMany
    @JoinTable(
            name = "path_courses",
            joinColumns = @JoinColumn(name = "path_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    private List<Course> courses;
}
