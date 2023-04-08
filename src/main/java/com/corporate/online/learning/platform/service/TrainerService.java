package com.corporate.online.learning.platform.service;

import com.corporate.online.learning.platform.dto.request.trainer.AddAssignmentsRequest;
import com.corporate.online.learning.platform.dto.request.trainer.CreatePathRequest;
import com.corporate.online.learning.platform.dto.request.trainer.RejectAssignmentRequest;
import com.corporate.online.learning.platform.dto.response.trainer.*;

import java.util.List;

public interface TrainerService {

    List<CoursesToTrainerResponse> showCourses(
            String name, String category, String sortBy, String sortMode, Long trainerId, Integer pageNo);

    void addAssignmentsToCourse(Long courseId, AddAssignmentsRequest request);

    List<CourseContentsToTrainerResponse> showCourseContents(Long courseId, Integer pageNo);

    CourseInfoToTrainerResponse showCourseInfo(Long courseId);

    List<EnrollmentsToTrainerResponse> showEnrollments(
            String name, String department, String position, String seniority,
            String sortBy, String sortMode, Long courseId, Integer pageNo);

    List<TraineeAssignmentsToTrainerResponse> showTraineeAssignments(
            Long traineeId, Long courseId, Integer pageNo);

    void rejectAssignment(Long assignmentStatsId, RejectAssignmentRequest request);

    List<PathToTrainerResponse> showPaths(
            String name, String category, String sortBy, String sortMode, Long trainerId, Integer pageNo);

    void deletePath(Long pathId);

    void createPath(CreatePathRequest request, Long trainerId);

    List<CoursesToTrainerResponse> showCoursesToAddInPath(
            String name, String category, String sortBy, String sortMode, Long trainerId, Integer pageNo);

    PathInfoToTrainerResponse showPathInfo(Long pathId);
}
