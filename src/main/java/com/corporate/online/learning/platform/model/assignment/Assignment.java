package com.corporate.online.learning.platform.model.assignment;

import com.corporate.online.learning.platform.model.course.Course;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "assignment")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "needs_grading", nullable = false)
    private Boolean needsGrading;
    @Column(nullable = false, columnDefinition="TEXT")
    private String text;
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    @OneToMany(mappedBy = "assignment", orphanRemoval = true)
    private List<AssignmentCompletionStats> assignmentCompletionStats;
}
