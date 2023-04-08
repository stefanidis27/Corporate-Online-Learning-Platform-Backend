package com.corporate.online.learning.platform.service;

import com.corporate.online.learning.platform.dto.request.hr.*;
import com.corporate.online.learning.platform.dto.response.hr.*;
import lombok.SneakyThrows;

import java.util.List;

public interface HRService {

    void changeAccountDetails(Long accountId, ChangeAccountDetailsRequest request);

    void createCourse(CreateCourseRequest request);

    List<TrainersToHRResponse> showTrainers(
            String name, String department, String position, String seniority,
            String email, Integer pageNo);

    void editCourse(Long courseId, EditCourseRequest request);

    List<CoursesToHRResponse> showCourses(
            String name, String category, String sortBy,
            String sortMode, Integer pageNo);

    List<TrainersReportResponse> showTrainersReport(
            String name, String department, String position, String seniority, List<String> courses,
            Long currentTraineesLow, Long currentTraineesHigh, Integer currentNoCoursesLow,
            Integer currentNoCoursesHigh, List<String> paths, Integer currentNoPathsLow,
            Integer currentNoPathsHigh, Integer pageNo);

    @SneakyThrows
    void createTrainersReport(Long hrId, CreateTrainersReportRequest request);

    List<CoursesReportResponse> showCoursesReport(
            String name, String category, String selfEnrollment, List<String> trainers, List<String> paths,
            Integer assignmentsNoLow, Integer assignmentsNoHigh, Long completionsNoLow, Long completionsNoHigh,
            Long unEnrollmentsNoLow, Long unEnrollmentsNoHigh, Long currentEnrollmentsNoLow,
            Long currentEnrollmentsNoHigh, Long possibleEnrollmentsNoLow, Long possibleEnrollmentsNoHigh,
            Float completionRateLow, Float completionRateHigh, Float dropOutRateLow, Float dropOutRateHigh,
            String sortBy, String sortMode, Integer pageNo);

    @SneakyThrows
    void createCoursesReport(Long hrId, CreateCoursesReportRequest request);

    void deleteCourse(Long courseId);

    List<EnrollmentsToHRResponse> showEnrollments(
            String name, String email, String department, String position,
            String seniority, Long courseId, Integer pageNo);

    List<EnrollmentsToManageResponse> showEnrollmentsForManagement(
            String name, String email, String department, String position,
            String seniority, Long courseId, Integer pageNo);

    CourseEnrollmentDetailsResponse getCourseEnrollmentDetails(Long courseId);

    List<TraineesReportResponse> showTraineesReport(
            String name, String department, String position, String seniority, String course,
            Float progressLevelLow, Float progressLevelHigh, String enrollmentDateEarliest,
            String enrollmentDateLatest, String path, Float progressPathLevelLow,
            Float progressPathLevelHigh, Integer pageNo);

    @SneakyThrows
    void createTraineesReport(Long hrId, CreateTraineesReportRequest request);

    List<PathReportResponse> showPathsReport(
            String name, String category, String trainer, Integer courseNoLow, Integer courseNoHigh,
            Long completionsNoLow, Long completionsNoHigh, Long currentEnrollmentsNoLow, Long currentEnrollmentsNoHigh,
            List<String> courses, String sortBy, String sortMode, Integer pageNo);

    void createPathsReport(Long hrId, CreatePathsReportRequest request);
}
