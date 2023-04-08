package com.corporate.online.learning.platform.repository.course;

import com.corporate.online.learning.platform.model.account.AccountDetails;
import com.corporate.online.learning.platform.model.course.Course;
import com.corporate.online.learning.platform.model.course.CourseCompletionStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseCompletionStatsRepository extends JpaRepository<CourseCompletionStats, Long> {

    List<CourseCompletionStats> findByAccountDetails(AccountDetails accountDetails, Pageable pageable);

    List<CourseCompletionStats> findByCourse(Course course, Pageable pageable);
}
