package com.corporate.online.learning.platform.controller;

import com.corporate.online.learning.platform.dto.response.trainee.*;
import com.corporate.online.learning.platform.exception.ErrorMessage;
import com.corporate.online.learning.platform.exception.path.PathCompletionStatsDeletionException;
import com.corporate.online.learning.platform.exception.path.PathException;
import com.corporate.online.learning.platform.service.CommonService;
import com.corporate.online.learning.platform.service.TraineeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/trainee")
public class TraineeController {

    private final TraineeService traineeService;
    private final CommonService commonService;

    @PostMapping("/{traineeId}/enroll-in-a-course/{courseId}")
    public ResponseEntity<Void> enrollInCourse(
            @PathVariable Long courseId,
            @PathVariable Long traineeId) {
        commonService.enrollInCourse(courseId, traineeId, Boolean.FALSE);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{traineeId}/un-enroll-from-a-course/{courseId}")
    public ResponseEntity<Void> unEnrollFromCourse(
            @PathVariable Long courseId,
            @PathVariable Long traineeId) {
        commonService.unEnrollFromCourse(courseId, traineeId, Boolean.FALSE);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{traineeId}/get-course-info/{courseId}")
    public ResponseEntity<CourseInfoToTraineeResponse> showCourseInfo(
            @PathVariable Long courseId,
            @PathVariable Long traineeId) {
        return ResponseEntity.ok(traineeService.showCourseInfo(courseId, traineeId));
    }

    @GetMapping("/{traineeId}/get-account-courses/{pageNo}")
    public ResponseEntity<List<AccountCoursesResponse>> showAccountCourses(
            @PathVariable Long traineeId,
            @PathVariable Integer pageNo,
            @RequestParam(defaultValue = "false") String completed) {
        return ResponseEntity.ok(traineeService.showCoursesToAccount(traineeId, pageNo, completed));
    }

    @GetMapping("/{traineeId}/get-courses/{pageNo}")
    public ResponseEntity<List<ExploreCoursesResponse>> showAllCourses(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "currentEnrollments") String sortBy,
            @RequestParam(defaultValue = "desc") String sortMode,
            @PathVariable Integer pageNo,
            @PathVariable Long traineeId) {
        return ResponseEntity.ok(traineeService.showAllCourses(name, category, sortBy, sortMode, pageNo, traineeId));
    }

    @GetMapping("/{traineeId}/get-course-enrollments/{courseId}/on-page/{pageNo}")
    public ResponseEntity<List<EnrollmentsToTraineeResponse>> showCourseEnrollments(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String seniority,
            @RequestParam(defaultValue = "enrollmentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortMode,
            @PathVariable Long traineeId,
            @PathVariable Long courseId,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(traineeService.showEnrollments(name, department, position, seniority, sortBy, sortMode,
                traineeId, courseId, pageNo));
    }

    @GetMapping("/{traineeId}/get-course-contents/{courseId}/on-page/{pageNo}")
    public ResponseEntity<List<AssignmentsToTraineeResponse>> showCourseContents(
            @PathVariable Long traineeId,
            @PathVariable Long courseId,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(traineeService.showCourseContents(traineeId, courseId, pageNo));
    }

    @PostMapping("/complete-assignment/{assignmentStatsId}")
    public ResponseEntity<Void> completeAssignment(@PathVariable Long assignmentStatsId) {
        commonService.completeAssignment(assignmentStatsId, Boolean.FALSE);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{traineeId}/get-account-paths/{pageNo}")
    public ResponseEntity<List<AccountPathsResponse>> showAccountPaths(
            @PathVariable Long traineeId,
            @PathVariable Integer pageNo,
            @RequestParam(defaultValue = "false") String completed) {
        return ResponseEntity.ok(traineeService.showPathsToAccount(traineeId, pageNo, completed));
    }

    @GetMapping("/{traineeId}/get-paths/{pageNo}")
    public ResponseEntity<List<ExplorePathsResponse>> showAllPaths(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "currentEnrollments") String sortBy,
            @RequestParam(defaultValue = "desc") String sortMode,
            @PathVariable Integer pageNo,
            @PathVariable Long traineeId) {
        return ResponseEntity.ok(traineeService.showAllPaths(name, category, sortBy, sortMode, pageNo, traineeId));
    }

    @GetMapping("/{traineeId}/get-path-info/{pathId}")
    public ResponseEntity<PathInfoToTraineeResponse> showPathInfo(
            @PathVariable Long pathId,
            @PathVariable Long traineeId) {
        return ResponseEntity.ok(traineeService.showPathInfo(pathId, traineeId));
    }

    @PostMapping("/{traineeId}/enroll-in-a-path/{pathId}")
    public ResponseEntity<Void> enrollInPath(
            @PathVariable Long pathId,
            @PathVariable Long traineeId) {
        traineeService.enrollInPath(pathId, traineeId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{traineeId}/un-enroll-from-a-path/{pathId}")
    public ResponseEntity<Void> unEnrollFromPath(
            @PathVariable Long pathId,
            @PathVariable Long traineeId) {
        traineeService.unEnrollFromPath(pathId, traineeId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @ExceptionHandler(PathCompletionStatsDeletionException.class)
    public final ResponseEntity<ErrorMessage> handlePathCompletionStatsDeletionException(
            PathCompletionStatsDeletionException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }
}
