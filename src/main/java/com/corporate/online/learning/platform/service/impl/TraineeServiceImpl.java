package com.corporate.online.learning.platform.service.impl;

import com.corporate.online.learning.platform.config.ApplicationConfig;
import com.corporate.online.learning.platform.dto.response.trainee.*;
import com.corporate.online.learning.platform.exception.account.AccountDetailsException;
import com.corporate.online.learning.platform.exception.account.AccountDetailsNotFoundException;
import com.corporate.online.learning.platform.exception.course.CourseNotFoundException;
import com.corporate.online.learning.platform.exception.path.*;
import com.corporate.online.learning.platform.model.account.AccountDetails;
import com.corporate.online.learning.platform.model.course.Course;
import com.corporate.online.learning.platform.model.course.CourseCompletionStats;
import com.corporate.online.learning.platform.model.path.Path;
import com.corporate.online.learning.platform.model.path.PathCompletionStats;
import com.corporate.online.learning.platform.repository.account.AccountDetailsRepository;
import com.corporate.online.learning.platform.repository.assignment.AssignmentCompletionStatsRepository;
import com.corporate.online.learning.platform.repository.course.CourseCompletionStatsRepository;
import com.corporate.online.learning.platform.repository.course.CourseRepository;
import com.corporate.online.learning.platform.repository.path.PathCompletionStatsRepository;
import com.corporate.online.learning.platform.repository.path.PathRepository;
import com.corporate.online.learning.platform.service.TraineeService;
import com.corporate.online.learning.platform.utils.PagingUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    private final PathRepository pathRepository;
    private final CourseRepository courseRepository;
    private final ApplicationConfig applicationConfig;
    private final PathCompletionStatsRepository pathStatsRepository;
    private final AccountDetailsRepository accountDetailsRepository;
    private final CourseCompletionStatsRepository courseStatsRepository;
    private final AssignmentCompletionStatsRepository assignmentStatsRepository;

    private enum EnrollmentAction {
        SELF_ENROLLMENT_NOT_ALLOWED,
        MAX_ENROLLMENTS_REACHED,
        ENROLLMENT_ALLOWED,
        COURSE_COMPLETED,
        UN_ENROLL
    }

    @Override
    public CourseInfoToTraineeResponse showCourseInfo(Long courseId, Long traineeId) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Trainee Course Info Error] No course with id "
                        + courseId + " found."));
        Boolean enrollmentStatus;
        String enrollmentAction, enrollmentDate = null;
        int completedAssignments;

        Optional<CourseCompletionStats> courseStats = course.getCourseCompletionStats().stream().filter(stats
                -> stats.getAccountDetails().getId().equals(traineeId)).findAny();
        if (courseStats.isEmpty()) {
            enrollmentStatus = Boolean.FALSE;
            completedAssignments = 0;
            if (course.getSelfEnrollment().equals(Boolean.FALSE)) {
                enrollmentAction = EnrollmentAction.SELF_ENROLLMENT_NOT_ALLOWED.toString();
            } else if (course.getMaxEnrollments().equals(course.getCurrentEnrollments())) {
                enrollmentAction = EnrollmentAction.MAX_ENROLLMENTS_REACHED.toString();
            } else {
                enrollmentAction = EnrollmentAction.ENROLLMENT_ALLOWED.toString();
            }
        } else {
            enrollmentStatus = Boolean.TRUE;
            enrollmentDate = courseStats.get().getEnrollmentDate().toString();
            completedAssignments = courseStats.get().getCompletedAssignments();
            if (courseStats.get().getCompletionStatus().equals(Boolean.TRUE)) {
                enrollmentAction = EnrollmentAction.COURSE_COMPLETED.toString();
            } else {
                enrollmentAction = EnrollmentAction.UN_ENROLL.toString();
            }
        }

        return CourseInfoToTraineeResponse.builder()
                .name(course.getName())
                .category(course.getCategory())
                .currentEnrollments(course.getCurrentEnrollments())
                .maxEnrollments(course.getMaxEnrollments())
                .trainers(course.getTrainersDetails().stream().map(AccountDetails::getName).toList())
                .numberOfAssignments(course.getNumberOfAssignments())
                .enrollmentStatus(enrollmentStatus)
                .completedAssignments(completedAssignments)
                .enrollmentAction(enrollmentAction)
                .enrollmentDate(enrollmentDate)
                .description(course.getDescription())
                .build();
    }

    @Override
    public List<AccountCoursesResponse> showCoursesToAccount(Long traineeId, Integer pageNo, String completed) {
        var account = accountDetailsRepository.findById(traineeId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Trainee Personal Course List Error] No account"
                        + " details with id " + traineeId + " found."));

        return courseStatsRepository.findByAccountDetails(account, PagingUtils.getPaging(pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(stats -> stats.getCompletionStatus().equals(Boolean.valueOf(completed)))
                .map(stats -> AccountCoursesResponse.builder()
                        .id(stats.getCourse().getId())
                        .name(stats.getCourse().getName())
                        .category(stats.getCourse().getCategory())
                        .numberOfAssignments(stats.getCourse().getNumberOfAssignments())
                        .completedAssignments(stats.getCompletedAssignments())
                        .build())
                .toList();
    }

    @Override
    public List<ExploreCoursesResponse> showAllCourses(
            String name, String category, String sortBy,
            String sortMode, Integer pageNo, Long traineeId) {
        var account = accountDetailsRepository.findById(traineeId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Trainee Course List Error] No account details "
                        + "with id " + traineeId + " found."));
        List<ExploreCoursesResponse> response = new ArrayList<>();

        List<Course> courses = courseRepository.findAll(PagingUtils.getPaging(sortBy, sortMode, pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(course -> (ObjectUtils.isEmpty(name) || course.getName()
                        .toUpperCase(Locale.ROOT).contains(name.toUpperCase(Locale.ROOT))))
                .filter(course -> (ObjectUtils.isEmpty(category) || course.getCategory()
                        .toUpperCase(Locale.ROOT).contains(category.toUpperCase(Locale.ROOT))))
                .filter(course -> course.getNumberOfAssignments() != 0)
                .toList();

        courses.forEach(course -> {
            var courseStats = account.getCourseCompletionStats().stream()
                    .filter(stats -> stats.getCourse().equals(course))
                    .findAny();
            if (courseStats.isPresent()) {
                response.add(ExploreCoursesResponse.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .category(course.getCategory())
                        .numberOfAssignments(course.getNumberOfAssignments())
                        .completedAssignments(courseStats.get().getCompletedAssignments())
                        .enrolmentStatus(Boolean.TRUE)
                        .build());
            } else {
                response.add(ExploreCoursesResponse.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .category(course.getCategory())
                        .numberOfAssignments(course.getNumberOfAssignments())
                        .enrolmentStatus(Boolean.FALSE)
                        .build());
            }
        });

        return response;
    }

    @Override
    public List<EnrollmentsToTraineeResponse> showEnrollments(
            String name, String department, String position, String seniority,
            String sortBy, String sortMode, Long traineeId, Long courseId, Integer pageNo) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Trainee Course Enrollments Error] No course with id "
                        + courseId + " found."));

        return courseStatsRepository.findByCourse(course, PagingUtils.getPaging(sortBy, sortMode, pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(stats -> !stats.getAccountDetails().getId().equals(traineeId))
                .filter(stats -> stats.getCompletionStatus().equals(Boolean.FALSE))
                .filter(stats -> (ObjectUtils.isEmpty(name) || stats.getAccountDetails().getName()
                        .toUpperCase(Locale.ROOT).contains(name.toUpperCase(Locale.ROOT))))
                .filter(stats -> (ObjectUtils.isEmpty(department) || stats.getAccountDetails().getDepartment()
                        .toUpperCase(Locale.ROOT).contains(department.toUpperCase(Locale.ROOT))))
                .filter(stats -> (ObjectUtils.isEmpty(seniority) || stats.getAccountDetails().getSeniority()
                        .toUpperCase(Locale.ROOT).contains(seniority.toUpperCase(Locale.ROOT))))
                .filter(stats -> (ObjectUtils.isEmpty(position) || stats.getAccountDetails().getPosition()
                        .toUpperCase(Locale.ROOT).contains(position.toUpperCase(Locale.ROOT))))
                .map(stats -> EnrollmentsToTraineeResponse.builder()
                        .name(stats.getAccountDetails().getName())
                        .enrollmentDate(stats.getEnrollmentDate().toString())
                        .completedAssignments(stats.getCompletedAssignments())
                        .numberOfAssignments(stats.getCourse().getNumberOfAssignments())
                        .build())
                .toList();
    }

    @Override
    public List<AssignmentsToTraineeResponse> showCourseContents(Long traineeId, Long courseId, Integer pageNo) {
        var account = accountDetailsRepository.findById(traineeId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Trainee Course Contents Error] No account "
                        + "details with id " + traineeId + " found."));
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Trainee Course Contents Error] No course with id "
                        + courseId + " found."));

        return assignmentStatsRepository.findByAccountDetails(account, PagingUtils.getPaging(pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(stats -> course.getAssignments().contains(stats.getAssignment()))
                .map(stats -> AssignmentsToTraineeResponse.builder()
                        .id(stats.getId())
                        .comment(stats.getComment())
                        .needsGrading(stats.getAssignment().getNeedsGrading())
                        .status(stats.getCompletionStatus())
                        .text(stats.getAssignment().getText())
                        .build())
                .toList();
    }

    @Override
    public List<AccountPathsResponse> showPathsToAccount(Long traineeId, Integer pageNo, String completed) {
        var account = accountDetailsRepository.findById(traineeId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Trainee Personal Paths List Error] No account "
                        + "details with id " + traineeId + " found."));

        return pathStatsRepository.findByAccountDetails(account, PagingUtils.getPaging(pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(stats -> stats.getCompletionStatus().equals(Boolean.valueOf(completed)))
                .map(stats -> AccountPathsResponse.builder()
                        .id(stats.getPath().getId())
                        .name(stats.getPath().getName())
                        .category(stats.getPath().getCategory())
                        .numberOfCourses(stats.getPath().getNumberOfCourses())
                        .completedCourses(stats.getCompletedCourses())
                        .build())
                .toList();
    }

    @Override
    public List<ExplorePathsResponse> showAllPaths(
            String name, String category, String sortBy, String sortMode, Integer pageNo, Long traineeId) {
        var account = accountDetailsRepository.findById(traineeId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Trainee Paths List Error] No account "
                        + "details with id " + traineeId + " found."));
        List<ExplorePathsResponse> response = new ArrayList<>();

        List<Path> paths = pathRepository.findAll(PagingUtils.getPaging(sortBy, sortMode, pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(path -> (ObjectUtils.isEmpty(name) || path.getName()
                        .toUpperCase(Locale.ROOT).contains(name.toUpperCase(Locale.ROOT))))
                .filter(path -> (ObjectUtils.isEmpty(category) || path.getCategory()
                        .toUpperCase(Locale.ROOT).contains(category.toUpperCase(Locale.ROOT))))
                .filter(path -> path.getNumberOfCourses() != 0)
                .toList();

        paths.forEach(path -> {
            var pathStats = account.getPathCompletionStats().stream()
                    .filter(stats -> stats.getPath().equals(path))
                    .findAny();
            if (pathStats.isPresent()) {
                response.add(ExplorePathsResponse.builder()
                        .id(path.getId())
                        .name(path.getName())
                        .category(path.getCategory())
                        .numberOfCourses(path.getNumberOfCourses())
                        .completedCourses(pathStats.get().getCompletedCourses())
                        .enrollmentStatus(Boolean.TRUE)
                        .build());
            } else {
                response.add(ExplorePathsResponse.builder()
                        .id(path.getId())
                        .name(path.getName())
                        .category(path.getCategory())
                        .numberOfCourses(path.getNumberOfCourses())
                        .enrollmentStatus(Boolean.FALSE)
                        .build());
            }
        });

        return response;
    }

    @Override
    public PathInfoToTraineeResponse showPathInfo(Long pathId, Long traineeId) {
        var path = pathRepository.findById(pathId)
                .orElseThrow(() -> new PathNotFoundException("[Trainee Path Info Error] No path with id " + pathId
                        + " found."));
        Boolean enrollmentStatus;
        int completedCourses;

        Optional<PathCompletionStats> pathStats = path.getPathCompletionStats().stream().filter(stats
                -> stats.getAccountDetails().getId().equals(traineeId)).findAny();
        if (pathStats.isEmpty()) {
            enrollmentStatus = Boolean.FALSE;
            completedCourses = 0;
        } else {
            enrollmentStatus = Boolean.TRUE;
            completedCourses = pathStats.get().getCompletedCourses();
        }

        List<ExploreCoursesResponse> courses = new ArrayList<>();
        path.getCourses().forEach(course -> {
            var courseStats = course.getCourseCompletionStats().stream()
                    .filter(stats -> stats.getAccountDetails().getId().equals(traineeId))
                    .findAny();
            if (courseStats.isPresent()) {
                courses.add(ExploreCoursesResponse.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .category(course.getCategory())
                        .numberOfAssignments(course.getNumberOfAssignments())
                        .completedAssignments(courseStats.get().getCompletedAssignments())
                        .enrolmentStatus(Boolean.TRUE)
                        .build());
            } else {
                courses.add(ExploreCoursesResponse.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .category(course.getCategory())
                        .numberOfAssignments(course.getNumberOfAssignments())
                        .enrolmentStatus(Boolean.FALSE)
                        .build());
            }
        });

        return PathInfoToTraineeResponse.builder()
                .name(path.getName())
                .category(path.getCategory())
                .currentEnrollments(path.getCurrentEnrollments())
                .trainer(ObjectUtils.isEmpty(path.getTrainerDetails()) ? "" : path.getTrainerDetails().getName())
                .numberOfCourses(path.getNumberOfCourses())
                .enrollmentStatus(enrollmentStatus)
                .completedCourses(completedCourses)
                .courses(courses)
                .build();
    }

    @Override
    public void enrollInPath(Long pathId, Long traineeId) {
        var account = accountDetailsRepository.findById(traineeId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Trainee Path Enrollment Error] No account "
                        + "details with id " + traineeId + " found."));
        var path = pathRepository.findById(pathId)
                .orElseThrow(() -> new PathNotFoundException("[Trainee Path Enrollment Error] No path with id " + pathId
                        + " found."));
        PathCompletionStats pathStats;

        if (checkIfTraineeCompletedAllCoursesInPath(account, path)) {
            path.setCompletions(path.getCompletions() + 1);
            pathStats = PathCompletionStats.builder()
                    .completionStatus(Boolean.TRUE)
                    .completedCourses(path.getNumberOfCourses())
                    .accountDetails(account)
                    .path(path)
                    .build();
        } else {
            path.setCurrentEnrollments(path.getCurrentEnrollments() + 1);
            pathStats = PathCompletionStats.builder()
                    .completionStatus(Boolean.FALSE)
                    .completedCourses(getNumberOfCompletedCourses(account, path))
                    .accountDetails(account)
                    .path(path)
                    .build();
        }
        path.getPathCompletionStats().add(pathStats);
        account.getPathCompletionStats().add(pathStats);

        try {
            pathStatsRepository.save(pathStats);
        } catch (DataAccessException e) {
            throw new PathCompletionStatsException("[Trainee Path Enrollment Error] Path completion stats could not be "
                    + "created for account with id " + traineeId + ".");
        }
        try {
            accountDetailsRepository.save(account);
        } catch (DataAccessException e) {
            throw new AccountDetailsException("[Trainee Path Enrollment Error] Account details could not be updated "
                    + "with the new path completion stats for account with id " + traineeId + ".");
        }
        try {
            pathRepository.save(path);
        } catch (DataAccessException e) {
            throw new PathException("[Trainee Path Enrollment Error] Path with id " + pathId + " could not be updated "
                    + "with the new path completion stats.");
        }
    }

    @Override
    public void unEnrollFromPath(Long pathId, Long traineeId) {
        var account = accountDetailsRepository.findById(traineeId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Trainee Path Un-enrolment Error] No account "
                        + "details with id " + traineeId + " found."));
        var path = pathRepository.findById(pathId)
                .orElseThrow(() -> new PathNotFoundException("[Trainee Path Un-enrolment Error] No path with id "
                        + pathId + " found."));
        var pathStats = account.getPathCompletionStats().stream()
                .filter(stats -> stats.getPath().equals(path))
                .findAny()
                .orElseThrow(() -> new PathCompletionStatsNotFoundException("[Trainee Path Un-enrolment Error] No path "
                        + "completion stats found for the account with id " + traineeId + "."));

        path.setCurrentEnrollments(path.getCurrentEnrollments() - 1);
        path.getPathCompletionStats().remove(pathStats);
        account.getPathCompletionStats().remove(pathStats);

        try {
            pathStatsRepository.delete(pathStats);
        } catch (DataAccessException e) {
            throw new PathCompletionStatsDeletionException("[Trainee Path Un-enrollment Error] Path completion stats "
                    + "with id " + pathStats.getId() + " could not be deleted.");
        }
        try {
            pathRepository.save(path);
        } catch (DataAccessException e) {
            throw new PathException("[Trainee Path Un-enrollment Error] Path with id " + pathId + " could not be "
                    + "updated with the new path completion stats.");
        }
        try {
            accountDetailsRepository.save(account);
        } catch (DataAccessException e) {
            throw new AccountDetailsException("[Trainee Path Un-enrollment Error] Account details could not be updated "
                    + "with the new path completion stats for account with id " + traineeId + ".");
        }
    }

    private Integer getNumberOfCompletedCourses(AccountDetails account, Path path) {
        return (int) path.getCourses().stream().map(course -> course.getCourseCompletionStats().stream()
                .filter(stats -> stats.getAccountDetails().equals(account))
                .findAny())
                .filter(completionStats -> completionStats.isPresent()
                        && completionStats.get().getCompletionStatus().equals(Boolean.TRUE)).count();
    }

    private boolean checkIfTraineeCompletedAllCoursesInPath(AccountDetails account, Path path) {
        final boolean[] check = {true};
        path.getCourses().forEach(course -> {
            Optional<CourseCompletionStats> completionStats = course.getCourseCompletionStats().stream()
                    .filter(stats -> stats.getAccountDetails().equals(account))
                    .findAny();

            if (completionStats.isEmpty() || completionStats.get().getCompletionStatus().equals(Boolean.FALSE)) {
                check[0] = false;
            }
        });

        return check[0];
    }
}
