package com.corporate.online.learning.platform.service;

import com.corporate.online.learning.platform.dto.response.trainee.*;

import java.util.List;

public interface TraineeService {

    CourseInfoToTraineeResponse showCourseInfo(Long courseId, Long traineeId);

    List<AccountCoursesResponse> showCoursesToAccount(Long traineeId, Integer pageNo, String completed);

    List<ExploreCoursesResponse> showAllCourses(
            String name, String category, String sortBy, String sortMode,
            Integer pageNo, Long traineeId);

    List<EnrollmentsToTraineeResponse> showEnrollments(
            String name, String department, String position, String seniority,
            String sortBy, String sortMode, Long traineeId, Long courseId, Integer pageNo);

    List<AssignmentsToTraineeResponse> showCourseContents(Long traineeId, Long courseId, Integer pageNo);

    List<AccountPathsResponse> showPathsToAccount(Long traineeId, Integer pageNo, String completed);

    List<ExplorePathsResponse> showAllPaths(
            String name, String category, String sortBy, String sortMode, Integer pageNo, Long traineeId);

    PathInfoToTraineeResponse showPathInfo(Long pathId, Long traineeId);

    void enrollInPath(Long pathId, Long traineeId);

    void unEnrollFromPath(Long pathId, Long traineeId);
}
