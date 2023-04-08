package com.corporate.online.learning.platform.controller;

import com.corporate.online.learning.platform.dto.request.hr.*;
import com.corporate.online.learning.platform.dto.response.common.AllAccountsResponse;
import com.corporate.online.learning.platform.dto.response.hr.*;
import com.corporate.online.learning.platform.exception.ErrorMessage;
import com.corporate.online.learning.platform.exception.course.CourseUniqueNameException;
import com.corporate.online.learning.platform.exception.course.CourseDeletionException;
import com.corporate.online.learning.platform.exception.report.ReportAttachmentException;
import com.corporate.online.learning.platform.exception.report.ReportCheckDateIntervalException;
import com.corporate.online.learning.platform.exception.report.ReportCreateTemporaryFileException;
import com.corporate.online.learning.platform.exception.report.ReportRemoveTemporaryFileException;
import com.corporate.online.learning.platform.service.CommonService;
import com.corporate.online.learning.platform.service.HRService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/hr")
public class HRController {

    private final CommonService commonService;
    private final HRService hrService;

    @GetMapping("/{hrId}/get-accounts/{pageNo}")
    public ResponseEntity<List<AllAccountsResponse>> showAccounts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String seniority,
            @RequestParam(required = false) String email,
            @PathVariable Long hrId,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(commonService.showAllAccounts(
                name, department, position,
                seniority, email, hrId, pageNo));
    }

    @PostMapping("/change-account-details/{accountId}")
    public ResponseEntity<Void> changeAccountDetails(
            @PathVariable Long accountId,
            @RequestBody ChangeAccountDetailsRequest request) {
        hrService.changeAccountDetails(accountId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/create-course")
    public ResponseEntity<Void> createCourse(
            @RequestBody CreateCourseRequest request) {
        hrService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/get-trainers/{pageNo}")
    public ResponseEntity<List<TrainersToHRResponse>> showTrainers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String seniority,
            @RequestParam(required = false) String email,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(hrService.showTrainers(name, department, position, seniority, email, pageNo));
    }

    @PostMapping("/edit-course/{courseId}")
    public ResponseEntity<Void> editCourse(
            @PathVariable Long courseId,
            @RequestBody EditCourseRequest request) {
        hrService.editCourse(courseId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/get-courses/{pageNo}")
    public ResponseEntity<List<CoursesToHRResponse>> showCourses(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "currentEnrollments") String sortBy,
            @RequestParam(defaultValue = "desc") String sortMode,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(hrService.showCourses(name, category, sortBy, sortMode, pageNo));
    }

    @GetMapping("/get-trainers-report/{pageNo}")
    public ResponseEntity<List<TrainersReportResponse>> showTrainersReport(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String seniority,
            @RequestParam(required = false) List<String> courses,
            @RequestParam(required = false) Long currentTraineesLow,
            @RequestParam(required = false) Long currentTraineesHigh,
            @RequestParam(required = false) Integer currentNoCoursesLow,
            @RequestParam(required = false) Integer currentNoCoursesHigh,
            @RequestParam(required = false) List<String> paths,
            @RequestParam(required = false) Integer currentNoPathsLow,
            @RequestParam(required = false) Integer currentNoPathsHigh,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(hrService.showTrainersReport(name, department, position, seniority, courses,
                currentTraineesLow, currentTraineesHigh, currentNoCoursesLow, currentNoCoursesHigh, paths,
                currentNoPathsLow, currentNoPathsHigh, pageNo));
    }

    @PostMapping("/{hrId}/create-trainers-report")
    public ResponseEntity<Void> createTrainersReport(
            @PathVariable Long hrId,
            @RequestBody CreateTrainersReportRequest request) {
        hrService.createTrainersReport(hrId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/get-courses-report/{pageNo}")
    public ResponseEntity<List<CoursesReportResponse>> showCoursesReport(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "any") String selfEnrollment,
            @RequestParam(required = false) List<String> trainers,
            @RequestParam(required = false) List<String> paths,
            @RequestParam(required = false) Integer assignmentsNoLow,
            @RequestParam(required = false) Integer assignmentsNoHigh,
            @RequestParam(required = false) Long completionsNoLow,
            @RequestParam(required = false) Long completionsNoHigh,
            @RequestParam(required = false) Long unEnrollmentsNoLow,
            @RequestParam(required = false) Long unEnrollmentsNoHigh,
            @RequestParam(required = false) Long currentEnrollmentsNoLow,
            @RequestParam(required = false) Long currentEnrollmentsNoHigh,
            @RequestParam(required = false) Long possibleEnrollmentsNoLow,
            @RequestParam(required = false) Long possibleEnrollmentsNoHigh,
            @RequestParam(required = false) Float completionRateLow,
            @RequestParam(required = false) Float completionRateHigh,
            @RequestParam(required = false) Float dropOutRateLow,
            @RequestParam(required = false) Float dropOutRateHigh,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortMode,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(hrService.showCoursesReport(name, category, selfEnrollment, trainers, paths,
                assignmentsNoLow, assignmentsNoHigh, completionsNoLow, completionsNoHigh, unEnrollmentsNoLow,
                unEnrollmentsNoHigh, currentEnrollmentsNoLow, currentEnrollmentsNoHigh, possibleEnrollmentsNoLow,
                possibleEnrollmentsNoHigh, completionRateLow, completionRateHigh, dropOutRateLow, dropOutRateHigh,
                sortBy, sortMode, pageNo));
    }

    @PostMapping("/{hrId}/create-courses-report")
    public ResponseEntity<Void> createCoursesReport(
            @PathVariable Long hrId,
            @RequestBody CreateCoursesReportRequest request) {
        hrService.createCoursesReport(hrId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/delete-course/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        hrService.deleteCourse(courseId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/get-course-enrollments/{courseId}/on-page/{pageNo}")
    public ResponseEntity<List<EnrollmentsToHRResponse>> showCourseEnrollments(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String seniority,
            @PathVariable Long courseId,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(hrService.showEnrollments(
                name, email, department, position, seniority, courseId, pageNo));
    }

    @GetMapping("/manage-enrollments/{courseId}/on-page/{pageNo}")
    public ResponseEntity<List<EnrollmentsToManageResponse>> showCourseEnrollmentsToManage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String seniority,
            @PathVariable Long courseId,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(hrService.showEnrollmentsForManagement(
                name, email, department, position, seniority, courseId, pageNo));
    }

    @GetMapping("/get-course-max-and-current-enrollments/{courseId}")
    public ResponseEntity<CourseEnrollmentDetailsResponse> showCourseEnrollmentDetails(@PathVariable Long courseId) {
        return ResponseEntity.ok(hrService.getCourseEnrollmentDetails(courseId));
    }

    @PostMapping("/enroll-trainee/{traineeId}/in-course/{courseId}")
    public ResponseEntity<Void> enrollInCourse(
            @PathVariable Long courseId,
            @PathVariable Long traineeId) {
        commonService.enrollInCourse(courseId, traineeId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/un-enroll-trainee/{traineeId}/from-course/{courseId}")
    public ResponseEntity<Void> unEnrollFromCourse(
            @PathVariable Long courseId,
            @PathVariable Long traineeId) {
        commonService.unEnrollFromCourse(courseId, traineeId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/get-trainees-report/{pageNo}")
    public ResponseEntity<List<TraineesReportResponse>> showTraineesReport(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String seniority,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) Float progressLevelLow,
            @RequestParam(required = false) Float progressLevelHigh,
            @RequestParam(required = false) String enrollmentDateEarliest,
            @RequestParam(required = false) String enrollmentDateLatest,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) Float progressPathLevelLow,
            @RequestParam(required = false) Float progressPathLevelHigh,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(hrService.showTraineesReport(name, department, position, seniority, course,
                progressLevelLow, progressLevelHigh, enrollmentDateEarliest, enrollmentDateLatest, path,
                progressPathLevelLow, progressPathLevelHigh, pageNo));
    }

    @PostMapping("/{hrId}/create-trainees-report")
    public ResponseEntity<Void> createTraineesReport(
            @PathVariable Long hrId,
            @RequestBody CreateTraineesReportRequest request) {
        hrService.createTraineesReport(hrId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/get-paths-report/{pageNo}")
    public ResponseEntity<List<PathReportResponse>> showPathsReport(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String trainer,
            @RequestParam(required = false) Integer courseNoLow,
            @RequestParam(required = false) Integer courseNoHigh,
            @RequestParam(required = false) Long completionsNoLow,
            @RequestParam(required = false) Long completionsNoHigh,
            @RequestParam(required = false) Long currentEnrollmentsNoLow,
            @RequestParam(required = false) Long currentEnrollmentsNoHigh,
            @RequestParam(required = false) List<String> courses,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortMode,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(hrService.showPathsReport(name, category, trainer, courseNoLow, courseNoHigh,
                completionsNoLow, completionsNoHigh, currentEnrollmentsNoLow, currentEnrollmentsNoHigh, courses,
                sortBy, sortMode, pageNo));
    }

    @PostMapping("/{hrId}/create-paths-report")
    public ResponseEntity<Void> createPathsReport(
            @PathVariable Long hrId,
            @RequestBody CreatePathsReportRequest request) {
        hrService.createPathsReport(hrId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ExceptionHandler(CourseDeletionException.class)
    public final ResponseEntity<ErrorMessage> handleCourseDeletionException(CourseDeletionException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(CourseUniqueNameException.class)
    public final ResponseEntity<ErrorMessage> handleCourseUniqueNameException(CourseUniqueNameException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(ReportAttachmentException.class)
    public final ResponseEntity<ErrorMessage> handleReportAttachmentException(ReportAttachmentException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(ReportRemoveTemporaryFileException.class)
    public final ResponseEntity<ErrorMessage> handleReportRemoveTemporaryFileException(
            ReportRemoveTemporaryFileException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(ReportCreateTemporaryFileException.class)
    public final ResponseEntity<ErrorMessage> handleReportCreateTemporaryFileException(
            ReportCreateTemporaryFileException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(ReportCheckDateIntervalException.class)
    public final ResponseEntity<ErrorMessage> handleReportCheckDateIntervalException(
            ReportCheckDateIntervalException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }
}
