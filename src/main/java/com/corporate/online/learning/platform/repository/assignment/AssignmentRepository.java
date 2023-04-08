package com.corporate.online.learning.platform.repository.assignment;

import com.corporate.online.learning.platform.model.assignment.Assignment;
import com.corporate.online.learning.platform.model.course.Course;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findAllByCourse(Course course, Pageable pageable);
}
