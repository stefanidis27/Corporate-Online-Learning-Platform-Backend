package com.corporate.online.learning.platform.repository.course;

import com.corporate.online.learning.platform.model.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
}
