package com.corporate.online.learning.platform.controller;

import com.corporate.online.learning.platform.dto.request.trainer.AddAssignmentsRequest;
import com.corporate.online.learning.platform.dto.request.trainer.CreatePathRequest;
import com.corporate.online.learning.platform.dto.request.trainer.RejectAssignmentRequest;
import com.corporate.online.learning.platform.dto.response.trainee.PathInfoToTraineeResponse;
import com.corporate.online.learning.platform.dto.response.trainer.*;
import com.corporate.online.learning.platform.exception.ErrorMessage;
import com.corporate.online.learning.platform.exception.assignment.AssignmentCompletionStatsDeletionException;
import com.corporate.online.learning.platform.exception.assignment.AssignmentException;
import com.corporate.online.learning.platform.exception.path.PathCreationException;
import com.corporate.online.learning.platform.exception.path.PathDeletionException;
import com.corporate.online.learning.platform.service.CommonService;
import com.corporate.online.learning.platform.service.TrainerService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/trainer")
public class TrainerController {

    private final TrainerService trainerService;
    private final CommonService commonService;

    @GetMapping("/{trainerId}/get-courses/{pageNo}")
    public ResponseEntity<List<CoursesToTrainerResponse>> showCourses(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "currentEnrollments") String sortBy,
            @RequestParam(defaultValue = "desc") String sortMode,
            @PathVariable Long trainerId,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(trainerService.showCourses(name, category, sortBy, sortMode, trainerId, pageNo));
    }

    @PostMapping("/add-assignments-to-course/{courseId}")
    public ResponseEntity<Void> addAssignmentsToCourse(
            @PathVariable Long courseId,
            @RequestBody AddAssignmentsRequest request) {
        trainerService.addAssignmentsToCourse(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/get-course-contents/{courseId}/on-page/{pageNo}")
    public ResponseEntity<List<CourseContentsToTrainerResponse>> showCourseContents(
            @PathVariable Long courseId,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(trainerService.showCourseContents(courseId, pageNo));
    }

    @GetMapping("/get-course-info/{courseId}")
    public ResponseEntity<CourseInfoToTrainerResponse> showCourseInfo(@PathVariable Long courseId) {
        return ResponseEntity.ok(trainerService.showCourseInfo(courseId));
    }

    @GetMapping("/get-course-enrollments/{courseId}/on-page/{pageNo}")
    public ResponseEntity<List<EnrollmentsToTrainerResponse>> showCourseEnrollments(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String seniority,
            @RequestParam(defaultValue = "enrollmentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortMode,
            @PathVariable Long courseId,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(trainerService.showEnrollments(name, department, position,
                seniority, sortBy, sortMode, courseId, pageNo));
    }

    @GetMapping("/get-trainee-assignments/{traineeId}/for-course/{courseId}/on-page/{pageNo}")
    public ResponseEntity<List<TraineeAssignmentsToTrainerResponse>> showTraineeAssignments(
            @PathVariable Long traineeId,
            @PathVariable Long courseId,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(trainerService.showTraineeAssignments(traineeId, courseId, pageNo));
    }

    @PostMapping("/reject-assignment/{assignmentStatsId}")
    public ResponseEntity<Void> rejectAssignment(
            @PathVariable Long assignmentStatsId,
            @RequestBody RejectAssignmentRequest request) {
        trainerService.rejectAssignment(assignmentStatsId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/approve-assignment/{assignmentStatsId}")
    public ResponseEntity<Void> approveAssignment(@PathVariable Long assignmentStatsId) {
        commonService.completeAssignment(assignmentStatsId, Boolean.TRUE);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{trainerId}/get-paths/{pageNo}")
    public ResponseEntity<List<PathToTrainerResponse>> showPaths(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "currentEnrollments") String sortBy,
            @RequestParam(defaultValue = "desc") String sortMode,
            @PathVariable Long trainerId,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(trainerService.showPaths(name, category, sortBy, sortMode, trainerId, pageNo));
    }

    @DeleteMapping("/delete-path/{pathId}")
    public ResponseEntity<Void> deletePath(@PathVariable Long pathId) {
        trainerService.deletePath(pathId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{trainerId}/create-path")
    public ResponseEntity<Void> createPath(@RequestBody CreatePathRequest request, @PathVariable Long trainerId) {
        trainerService.createPath(request, trainerId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{trainerId}/get-courses-path/{pageNo}")
    public ResponseEntity<List<CoursesToTrainerResponse>> showCoursesToAddInPath(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "currentEnrollments") String sortBy,
            @RequestParam(defaultValue = "desc") String sortMode,
            @PathVariable Long trainerId,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(trainerService.showCoursesToAddInPath(
                name, category, sortBy, sortMode, trainerId, pageNo));
    }

    @GetMapping("/get-path-info/{pathId}")
    public ResponseEntity<PathInfoToTrainerResponse> showPathInfo(@PathVariable Long pathId) {
        return ResponseEntity.ok(trainerService.showPathInfo(pathId));
    }

    @ExceptionHandler(AssignmentException.class)
    public final ResponseEntity<ErrorMessage> handleAssignmentException(AssignmentException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(PathDeletionException.class)
    public final ResponseEntity<ErrorMessage> handlePathDeletionException(PathDeletionException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(PathCreationException.class)
    public final ResponseEntity<ErrorMessage> handlePathCreationException(PathCreationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage(e.getMessage()));
    }
}
