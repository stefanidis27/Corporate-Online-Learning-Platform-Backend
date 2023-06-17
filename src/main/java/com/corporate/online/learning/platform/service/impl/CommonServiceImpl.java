package com.corporate.online.learning.platform.service.impl;

import com.corporate.online.learning.platform.config.ApplicationConfig;
import com.corporate.online.learning.platform.dto.response.common.AllAccountsResponse;
import com.corporate.online.learning.platform.dto.response.common.PersonalInfoResponse;
import com.corporate.online.learning.platform.dto.response.common.WebsiteInfoResponse;
import com.corporate.online.learning.platform.exception.account.AccountDetailsException;
import com.corporate.online.learning.platform.exception.account.AccountDetailsNotFoundException;
import com.corporate.online.learning.platform.exception.assignment.AssignmentCompletionStatsDeletionException;
import com.corporate.online.learning.platform.exception.assignment.AssignmentCompletionStatsException;
import com.corporate.online.learning.platform.exception.assignment.AssignmentCompletionStatsNotFoundException;
import com.corporate.online.learning.platform.exception.course.*;
import com.corporate.online.learning.platform.exception.path.PathCompletionStatsException;
import com.corporate.online.learning.platform.exception.path.PathException;
import com.corporate.online.learning.platform.model.account.AccountDetails;
import com.corporate.online.learning.platform.model.assignment.AssignmentCompletionStats;
import com.corporate.online.learning.platform.model.course.CourseCompletionStats;
import com.corporate.online.learning.platform.repository.account.AccountDetailsRepository;
import com.corporate.online.learning.platform.repository.assignment.AssignmentCompletionStatsRepository;
import com.corporate.online.learning.platform.repository.course.CourseCompletionStatsRepository;
import com.corporate.online.learning.platform.repository.course.CourseRepository;
import com.corporate.online.learning.platform.repository.path.PathCompletionStatsRepository;
import com.corporate.online.learning.platform.repository.path.PathRepository;
import com.corporate.online.learning.platform.service.CommonService;
import com.corporate.online.learning.platform.service.EmailService;
import com.corporate.online.learning.platform.utils.PagingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService {

    private final AccountDetailsRepository accountDetailsRepository;
    private final CourseRepository courseRepository;
    private final PathRepository pathRepository;
    private final EmailService emailService;
    private final PathCompletionStatsRepository pathStatsRepository;
    private final AssignmentCompletionStatsRepository assignmentStatsRepository;
    private final CourseCompletionStatsRepository courseStatsRepository;
    private final ApplicationConfig applicationConfig;

    @Override
    public PersonalInfoResponse getPersonalInfo(Long id) {
        AccountDetails accountDetails = accountDetailsRepository.findById(id)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Personal Info Retrieval Error] No account" +
                        " details with id " + id + " found."));

        return PersonalInfoResponse.builder()
                .name(accountDetails.getName())
                .department(accountDetails.getDepartment())
                .position(accountDetails.getPosition())
                .seniority(accountDetails.getSeniority())
                .build();
    }

    @Override
    public WebsiteInfoResponse getWebsiteDescription() {
        return WebsiteInfoResponse.builder().description(applicationConfig.getWebsiteDescription()).build();
    }

    @Override
    public List<AllAccountsResponse> showAllAccounts(
            String name, String department, String position, String seniority,
            String email, Long id, Integer pageNo) {

        return accountDetailsRepository.findAll(PagingUtils.getPaging(pageNo, applicationConfig.getFixedPageSize()))
                .stream()
                .filter(account -> !account.getId().equals(id))
                .filter(account -> (ObjectUtils.isEmpty(email) || account.getAccount().getEmail()
                        .toUpperCase(Locale.ROOT).contains(email.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(name) || account.getName()
                        .toUpperCase(Locale.ROOT).contains(name.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(department) || account.getDepartment()
                        .toUpperCase(Locale.ROOT).contains(department.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(seniority) || account.getSeniority()
                        .toUpperCase(Locale.ROOT).contains(seniority.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(position) || account.getPosition()
                        .toUpperCase(Locale.ROOT).contains(position.toUpperCase(Locale.ROOT))))
                .map(account -> AllAccountsResponse.builder()
                        .id(account.getId())
                        .email(account.getAccount().getEmail())
                        .name(account.getName())
                        .department(account.getDepartment())
                        .position(account.getPosition())
                        .seniority(account.getSeniority())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void completeAssignment(Long assignmentStatsId, Boolean sendConfirmationEmail) {
        var assignmentStats = assignmentStatsRepository.findById(assignmentStatsId)
                .orElseThrow(() -> new AssignmentCompletionStatsNotFoundException("[Assignment Completion Error] No " +
                        "assignment completion stats with id " + assignmentStatsId + " found."));
        var course = assignmentStats.getAssignment().getCourse();
        var account = assignmentStats.getAccountDetails();
        var courseStats = account.getCourseCompletionStats().stream()
                .filter(stats -> stats.getCourse().equals(course))
                .findAny()
                .orElseThrow(() -> new CourseCompletionStatsNotFoundException(
                        "[Assignment Completion Error] No course completion stats found for the account with id "
                                + account.getId() + "."));

        assignmentStats.setCompletionStatus(Boolean.TRUE);
        courseStats.setCompletedAssignments(courseStats.getCompletedAssignments() + 1);
        if (courseStats.getCompletedAssignments().equals(course.getNumberOfAssignments())) {
            courseStats.setCompletionStatus(Boolean.TRUE);
            course.setCurrentEnrollments(course.getCurrentEnrollments() - 1);
            course.setCompletions(course.getCompletions() + 1);
            try {
                courseRepository.save(course);
            } catch (DataAccessException e) {
                throw new CourseException("[Assignment Completion Error] Course with id " + course.getId()
                        + " could not be updated with the new completion stats.");
            }
            if (sendConfirmationEmail) {
                emailService.sendEmailCourseCompletedConfirmation(courseStats);
            }

            course.getPaths().forEach(path -> path.getPathCompletionStats().forEach(stats -> {
                stats.setCompletedCourses(stats.getCompletedCourses() + 1);
                if (stats.getCompletedCourses().equals(path.getNumberOfCourses())) {
                    stats.setCompletionStatus(Boolean.TRUE);
                    path.setCurrentEnrollments(path.getCurrentEnrollments() - 1);
                    path.setCompletions(path.getCompletions() + 1);
                    try {
                        pathRepository.save(path);
                    } catch (DataAccessException e) {
                        throw new PathException("[Assignment Completion Error] Path with id " + path.getId()
                                + " could not be updated with the new completion stats.");
                    }
                }
                try {
                    pathStatsRepository.save(stats);
                } catch (DataAccessException e) {
                    throw new PathCompletionStatsException("[Assignment Completion Error] Path completion stats with" +
                            " id " + stats.getId() + " could not be updated.");
                }
            }));
        }

        try {
            assignmentStatsRepository.save(assignmentStats);
        } catch (DataAccessException e) {
            throw new AssignmentCompletionStatsException("[Assignment Completion Error] Assignment completion stats " +
                    "with id " + assignmentStats.getId() + " could not be updated.");
        }
        try {
            courseStatsRepository.save(courseStats);
        } catch (DataAccessException e) {
            throw new CourseCompletionStatsException("[Assignment Completion Error] Course completion stats with" +
                    " id " + courseStats.getId() + " could not be updated.");
        }
        if (sendConfirmationEmail) {
            emailService.sendEmailApprovedAssignmentConfirmation(assignmentStats);
        }
    }

    @Override
    public void enrollInCourse(Long courseId, Long traineeId, Boolean sendConfirmationEmail) {
        var account = accountDetailsRepository.findById(traineeId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Enrollment Error] No account with id "
                        + traineeId + " found."));
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Enrollment Error] No course with id " + courseId
                        + " found."));

        course.setCurrentEnrollments(course.getCurrentEnrollments() + 1);
        Date date = new Date(System.currentTimeMillis());
        var courseStats = CourseCompletionStats.builder()
                .completionStatus(Boolean.FALSE)
                .completedAssignments(0)
                .enrollmentDate(new Timestamp(date.getTime()))
                .accountDetails(account)
                .course(course)
                .build();
        course.getCourseCompletionStats().add(courseStats);
        account.getCourseCompletionStats().add(courseStats);

        course.getAssignments().forEach(assignment -> {
            var assignmentStats = AssignmentCompletionStats.builder()
                    .completionStatus(Boolean.FALSE)
                    .assignment(assignment)
                    .accountDetails(account)
                    .build();
            assignment.getAssignmentCompletionStats().add(assignmentStats);
            account.getAssignmentCompletionStats().add(assignmentStats);
            try {
                assignmentStatsRepository.save(assignmentStats);
            } catch (DataAccessException e) {
                throw new AssignmentCompletionStatsException("[Enrollment Error] Assignment completion stats could not"
                        + " be created.");
            }
        });

        try {
            courseStatsRepository.save(courseStats);
        } catch (DataAccessException e) {
            throw new CourseCompletionStatsException("[Enrollment Error] Course completion stats with id "
                    + courseStats.getId() + " could not be updated.");
        }
        try {
            accountDetailsRepository.save(account);
        } catch (DataAccessException e) {
            throw new AccountDetailsException("[Enrollment Error] Account details with id "
                    + account.getId() + " could not be updated with the new completion stats.");
        }
        try {
            courseRepository.save(course);
        } catch (DataAccessException e) {
            throw new CourseException("[Enrollment Error] Course with id "
                    + course.getId() + " could not be updated with the new completion stats.");
        }

        if (sendConfirmationEmail) {
            emailService.sendEmailCourseEnrollmentConfirmation(course.getName(), account.getAccount().getEmail());
        }
    }

    @Override
    public void unEnrollFromCourse(Long courseId, Long traineeId, Boolean sendConfirmationEmail) {
        var account = accountDetailsRepository.findById(traineeId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[Un-enrollment Error] No account with id "
                        + traineeId + " found."));
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Un-enrollment Error] No course with id " + courseId
                        + " found."));
        var courseStats = account.getCourseCompletionStats().stream()
                .filter(stats -> stats.getCourse().equals(course))
                .findAny()
                .orElseThrow(() -> new CourseCompletionStatsNotFoundException("[Un-enrollment Error] No course " +
                        "completion stats found for the course with id " + courseId + "."));
        var assignmentStats = account.getAssignmentCompletionStats().stream()
                .filter(stats -> course.getAssignments().contains(stats.getAssignment()))
                .toList();

        course.setCurrentEnrollments(course.getCurrentEnrollments() - 1);
        course.setUnEnrollments(course.getUnEnrollments() + 1);
        course.getCourseCompletionStats().remove(courseStats);
        course.getAssignments().forEach(assignment -> assignment.getAssignmentCompletionStats()
                .removeIf(stats -> stats.getAccountDetails().equals(account)));

        account.getAssignmentCompletionStats().removeAll(assignmentStats);
        account.getCourseCompletionStats().remove(courseStats);

        try {
            courseStatsRepository.delete(courseStats);
        } catch (DataAccessException e) {
            throw new CourseCompletionStatsDeletionException("[Un-enrollment Error] Course completion stats with id "
                + courseStats.getId() + " could not be deleted.");
        }
        try {
            assignmentStatsRepository.deleteAll(assignmentStats);
        } catch (DataAccessException e) {
            throw new AssignmentCompletionStatsDeletionException("[Un-enrollment Error] Assignment completion stats "
                    + "could not be deleted.");
        }
        try {
            courseRepository.save(course);
        } catch (DataAccessException e) {
            throw new CourseException("[Un-enrollment Error] Course with id "
                    + course.getId() + " could not be updated with the new completion stats.");
        }
        try {
            accountDetailsRepository.save(account);
        } catch (DataAccessException e) {
            throw new AccountDetailsException("[Un-enrollment Error] Account details with id "
                    + account.getId() + " could not be updated with the new completion stats.");
        }
        if (sendConfirmationEmail) {
            emailService.sendEmailCourseUnEnrollmentConfirmation(course.getName(), account.getAccount().getEmail());
        }
    }
}
