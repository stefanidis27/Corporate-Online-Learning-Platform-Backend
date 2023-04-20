package com.corporate.online.learning.platform.service.impl;

import com.corporate.online.learning.platform.config.ApplicationConfig;
import com.corporate.online.learning.platform.dto.request.trainer.AddAssignmentsRequest;
import com.corporate.online.learning.platform.dto.request.trainer.CreatePathRequest;
import com.corporate.online.learning.platform.dto.request.trainer.RejectAssignmentRequest;
import com.corporate.online.learning.platform.dto.response.trainer.*;
import com.corporate.online.learning.platform.exception.account.AccountDetailsNotFoundException;
import com.corporate.online.learning.platform.exception.assignment.AssignmentCompletionStatsException;
import com.corporate.online.learning.platform.exception.assignment.AssignmentCompletionStatsNotFoundException;
import com.corporate.online.learning.platform.exception.assignment.AssignmentException;
import com.corporate.online.learning.platform.exception.course.CourseException;
import com.corporate.online.learning.platform.exception.course.CourseNotFoundException;
import com.corporate.online.learning.platform.exception.path.PathCreationException;
import com.corporate.online.learning.platform.exception.path.PathDeletionException;
import com.corporate.online.learning.platform.exception.path.PathNotFoundException;
import com.corporate.online.learning.platform.model.account.AccountDetails;
import com.corporate.online.learning.platform.model.assignment.Assignment;
import com.corporate.online.learning.platform.model.course.Course;
import com.corporate.online.learning.platform.model.path.Path;
import com.corporate.online.learning.platform.repository.account.AccountDetailsRepository;
import com.corporate.online.learning.platform.repository.assignment.AssignmentCompletionStatsRepository;
import com.corporate.online.learning.platform.repository.assignment.AssignmentRepository;
import com.corporate.online.learning.platform.repository.course.CourseCompletionStatsRepository;
import com.corporate.online.learning.platform.repository.course.CourseRepository;
import com.corporate.online.learning.platform.repository.path.PathRepository;
import com.corporate.online.learning.platform.service.EmailService;
import com.corporate.online.learning.platform.service.TrainerService;
import com.corporate.online.learning.platform.utils.PagingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {

    private final CourseRepository courseRepository;
    private final PathRepository pathRepository;
    private final EmailService emailService;
    private final ApplicationConfig applicationConfig;
    private final AssignmentRepository assignmentRepository;
    private final AccountDetailsRepository accountDetailsRepository;
    private final CourseCompletionStatsRepository courseStatsRepository;
    private final AssignmentCompletionStatsRepository assignmentStatsRepository;

    @Override
    public List<CoursesToTrainerResponse> showCourses(
            String name, String category, String sortBy, String sortMode, Long trainerId, Integer pageNo) {
        var trainerDetails = accountDetailsRepository.findById(trainerId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Trainer Course List Error] No account"
                        + " details with id " + trainerId + " found."));

        return courseRepository.findAll(PagingUtils.getPaging(sortBy, sortMode, pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(course -> (ObjectUtils.isEmpty(name) || course.getName()
                        .toUpperCase(Locale.ROOT).contains(name.toUpperCase(Locale.ROOT))))
                .filter(course -> (ObjectUtils.isEmpty(category) || course.getCategory()
                        .toUpperCase(Locale.ROOT).contains(category.toUpperCase(Locale.ROOT))))
                .filter(course -> course.getTrainersDetails().contains(trainerDetails))
                .filter(course -> course.getNumberOfAssignments() == 0 || course.getCurrentEnrollments() != 0)
                .map(course -> CoursesToTrainerResponse.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .category(course.getCategory())
                        .noAssignments(course.getNumberOfAssignments())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void addAssignmentsToCourse(Long courseId, AddAssignmentsRequest request) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Trainer Assignment Adding Error] No course with id "
                        + courseId + " found."));
        List<Assignment> assignments = new ArrayList<>();
        request.getRequestList().forEach(createAssignmentRequest ->
            assignments.add(Assignment.builder()
                    .course(course)
                    .needsGrading(createAssignmentRequest.getNeedsGrading())
                    .text(createAssignmentRequest.getText())
                    .assignmentCompletionStats(new ArrayList<>())
                    .build())
        );

        course.getAssignments().addAll(assignments);
        course.setNumberOfAssignments(assignments.size());
        try {
            courseRepository.save(course);
        } catch (DataAccessException e) {
            throw new CourseException("[Trainer Assignment Adding Error] Course with id " + courseId
                    + " could not be updated with the new assignments.");
        }
        try {
            assignmentRepository.saveAll(assignments);
        } catch (DataAccessException e) {
            throw new AssignmentException("[Trainer Assignment Adding Error] Assignments could not be created for the "
                    + "course with id " + courseId + ".");
        }
    }

    @Override
    public List<CourseContentsToTrainerResponse> showCourseContents(Long courseId, Integer pageNo) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Trainer Course Contents Error] No course with id "
                        + courseId + " found."));

        return assignmentRepository.findAllByCourse(course, PagingUtils.getPaging(pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .map(assignment -> CourseContentsToTrainerResponse.builder()
                        .needsGrading(assignment.getNeedsGrading())
                        .text(assignment.getText())
                        .build())
                .toList();
    }

    @Override
    public CourseInfoToTrainerResponse showCourseInfo(Long courseId) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Trainer Course Info Error] No course with id "
                        + courseId + " found."));

        return CourseInfoToTrainerResponse.builder()
                .name(course.getName())
                .category(course.getCategory())
                .currentEnrollments(course.getCurrentEnrollments())
                .maxEnrollments(course.getMaxEnrollments())
                .trainers(course.getTrainersDetails().stream().map(AccountDetails::getName).toList())
                .description(course.getDescription())
                .build();
    }

    @Override
    public List<EnrollmentsToTrainerResponse> showEnrollments(
            String name, String department, String position, String seniority,
            String sortBy, String sortMode, Long courseId, Integer pageNo) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Trainer Course Enrollments Error] No course with id "
                        + courseId + " found."));

        return courseStatsRepository.findByCourse(course, PagingUtils.getPaging(sortBy, sortMode, pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(stats -> stats.getCompletionStatus().equals(Boolean.FALSE))
                .filter(stats -> (ObjectUtils.isEmpty(name) || stats.getAccountDetails().getName()
                        .toUpperCase(Locale.ROOT).contains(name.toUpperCase(Locale.ROOT))))
                .filter(stats -> (ObjectUtils.isEmpty(department) || stats.getAccountDetails().getDepartment()
                        .toUpperCase(Locale.ROOT).contains(department.toUpperCase(Locale.ROOT))))
                .filter(stats -> (ObjectUtils.isEmpty(seniority) || stats.getAccountDetails().getSeniority()
                        .toUpperCase(Locale.ROOT).contains(seniority.toUpperCase(Locale.ROOT))))
                .filter(stats -> (ObjectUtils.isEmpty(position) || stats.getAccountDetails().getPosition()
                        .toUpperCase(Locale.ROOT).contains(position.toUpperCase(Locale.ROOT))))
                .map(stats -> EnrollmentsToTrainerResponse.builder()
                        .id(stats.getAccountDetails().getId())
                        .name(stats.getAccountDetails().getName())
                        .enrollmentDate(stats.getEnrollmentDate().toString())
                        .completedAssignments(stats.getCompletedAssignments())
                        .numberOfAssignments(stats.getCourse().getNumberOfAssignments())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<TraineeAssignmentsToTrainerResponse> showTraineeAssignments(
            Long traineeId, Long courseId, Integer pageNo) {
        var account = accountDetailsRepository.findById(traineeId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Trainer Trainee Assignments Error] No trainee "
                        + "account details with id " + traineeId + " found."));
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Trainer Trainee Assignments Error] No course with id "
                        + courseId + " found."));

        return assignmentStatsRepository.findByAccountDetails(account, PagingUtils.getPaging(pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(stats -> course.getAssignments().contains(stats.getAssignment()))
                .filter(stats -> stats.getCompletionStatus().equals(Boolean.FALSE))
                .filter(stats -> stats.getAssignment().getNeedsGrading().equals(Boolean.TRUE))
                .map(stats -> TraineeAssignmentsToTrainerResponse.builder()
                        .id(stats.getId())
                        .text(stats.getAssignment().getText())
                        .comment(stats.getComment())
                        .build())
                .toList();
    }

    @Override
    public void rejectAssignment(Long assignmentStatsId, RejectAssignmentRequest request) {
        var assignmentStats = assignmentStatsRepository.findById(assignmentStatsId)
                .orElseThrow(() -> new AssignmentCompletionStatsNotFoundException("[Trainer Reject Assignment Error] "
                        + "No assignment completion stats with id " + assignmentStatsId + " found."));
        assignmentStats.setComment(request.getComment());
        try {
            assignmentStatsRepository.save(assignmentStats);
        } catch (DataAccessException e) {
            throw new AssignmentCompletionStatsException("[Trainer Reject Assignment Error] Assignment completion "
                    + "stats with id " + assignmentStatsId + " could not be updated.");
        }

        emailService.sendEmailRejectedAssignment(assignmentStats);
    }

    @Override
    public List<PathToTrainerResponse> showPaths(
            String name, String category, String sortBy, String sortMode, Long trainerId, Integer pageNo) {
        var account = accountDetailsRepository.findById(trainerId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Trainer Path List Error] No account"
                        + " details with id " + trainerId + " found."));

        return pathRepository.findAll(PagingUtils.getPaging(sortBy, sortMode, pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(path -> !ObjectUtils.isEmpty(path.getTrainerDetails())
                        && path.getTrainerDetails().equals(account))
                .filter(path -> (ObjectUtils.isEmpty(name) || path.getName()
                        .toUpperCase(Locale.ROOT).contains(name.toUpperCase(Locale.ROOT))))
                .filter(path -> (ObjectUtils.isEmpty(category) || path.getCategory()
                        .toUpperCase(Locale.ROOT).contains(category.toUpperCase(Locale.ROOT))))
                .map(path -> PathToTrainerResponse.builder()
                        .id(path.getId())
                        .name(path.getName())
                        .category(path.getCategory())
                        .currentEnrollments(path.getCurrentEnrollments())
                        .noCourses(path.getNumberOfCourses())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void deletePath(Long pathId) {
        var path = pathRepository.findById(pathId)
                .orElseThrow(() -> new PathNotFoundException("[Trainer Path Deletion Error] No path found with id "
                        + pathId + " found."));
        if (!ObjectUtils.isEmpty(path.getTrainerDetails())) {
            path.getTrainerDetails().getCreatedPaths().remove(path);
        }
        path.getCourses().forEach(course -> course.getPaths().remove(path));

        try {
            pathRepository.delete(path);
        } catch (DataAccessException e) {
            throw new PathDeletionException("[Trainer Path Deletion Error] Path with id " + pathId
                    + " could not be deleted.");
        }
    }

    @Override
    public void createPath(CreatePathRequest request, Long trainerId) {
        var account = accountDetailsRepository.findById(trainerId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Trainer Path Creation Error] No account"
                        + " details with id " + trainerId + " found."));
        List<Course> courses = courseRepository.findAllById(request.getCourseIds());

        Path path = Path.builder()
                .name(request.getName())
                .category(request.getCategory())
                .completions(0L)
                .currentEnrollments(0L)
                .numberOfCourses(request.getCourseIds().size())
                .pathCompletionStats(new ArrayList<>())
                .courses(courses)
                .trainerDetails(account)
                .build();

        account.getCreatedPaths().add(path);
        try {
            pathRepository.save(path);
        } catch (DataAccessException e) {
            throw new PathCreationException("[Trainer Path Creation Error] Path could not be created.");
        }
    }

    @Override
    public List<CoursesToTrainerResponse> showCoursesToAddInPath(
            String name, String category, String sortBy, String sortMode, Long trainerId, Integer pageNo) {
        var account = accountDetailsRepository.findById(trainerId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Trainer Course List Path Error] No account"
                        + " details with id " + trainerId + " found."));

        return courseRepository.findAll(PagingUtils.getPaging(sortBy, sortMode, pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(course -> (ObjectUtils.isEmpty(name) || course.getName()
                        .toUpperCase(Locale.ROOT).contains(name.toUpperCase(Locale.ROOT))))
                .filter(course -> (ObjectUtils.isEmpty(category) || course.getCategory()
                        .toUpperCase(Locale.ROOT).contains(category.toUpperCase(Locale.ROOT))))
                .filter(course -> course.getNumberOfAssignments() != 0)
                .filter(course -> course.getTrainersDetails().contains(account))
                .map(course -> CoursesToTrainerResponse.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .category(course.getCategory())
                        .noAssignments(course.getNumberOfAssignments())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public PathInfoToTrainerResponse showPathInfo(Long pathId) {
        var path = pathRepository.findById(pathId)
                .orElseThrow(() -> new PathNotFoundException("[Trainer Path Info Error] No path found with id " + pathId
                        + " found."));
        List<CoursesToTrainerResponse> courses = courseRepository.findAll().stream()
                .filter(course -> course.getPaths().contains(path))
                .map(course -> CoursesToTrainerResponse.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .category(course.getCategory())
                        .noAssignments(course.getNumberOfAssignments())
                        .build())
                .collect(Collectors.toList());

        return PathInfoToTrainerResponse.builder()
                .name(path.getName())
                .category(path.getCategory())
                .currentEnrollments(path.getCurrentEnrollments())
                .courses(courses)
                .build();
    }
}
