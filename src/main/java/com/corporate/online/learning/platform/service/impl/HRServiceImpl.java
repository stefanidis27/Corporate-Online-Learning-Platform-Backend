package com.corporate.online.learning.platform.service.impl;

import com.corporate.online.learning.platform.config.ApplicationConfig;
import com.corporate.online.learning.platform.dto.request.hr.*;
import com.corporate.online.learning.platform.dto.response.hr.*;
import com.corporate.online.learning.platform.exception.account.AccountDetailsException;
import com.corporate.online.learning.platform.exception.account.AccountDetailsNotFoundException;
import com.corporate.online.learning.platform.exception.course.CourseDeletionException;
import com.corporate.online.learning.platform.exception.course.CourseNotFoundException;
import com.corporate.online.learning.platform.exception.course.CourseUniqueNameException;
import com.corporate.online.learning.platform.exception.path.PathCompletionStatsNotFoundException;
import com.corporate.online.learning.platform.exception.report.ReportAttachmentException;
import com.corporate.online.learning.platform.exception.report.ReportRemoveTemporaryFileException;
import com.corporate.online.learning.platform.model.account.AccountDetails;
import com.corporate.online.learning.platform.model.account.Role;
import com.corporate.online.learning.platform.model.course.Course;
import com.corporate.online.learning.platform.model.course.CourseCompletionStats;
import com.corporate.online.learning.platform.model.path.Path;
import com.corporate.online.learning.platform.model.path.PathCompletionStats;
import com.corporate.online.learning.platform.repository.account.AccountDetailsRepository;
import com.corporate.online.learning.platform.repository.course.CourseCompletionStatsRepository;
import com.corporate.online.learning.platform.repository.course.CourseRepository;
import com.corporate.online.learning.platform.repository.path.PathRepository;
import com.corporate.online.learning.platform.service.HRService;
import com.corporate.online.learning.platform.utils.CSVUtils;
import com.corporate.online.learning.platform.utils.CheckIntervalUtils;
import com.corporate.online.learning.platform.utils.PagingUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HRServiceImpl implements HRService {

    private final AccountDetailsRepository accountDetailsRepository;
    private final CourseCompletionStatsRepository courseStatsRepository;
    private final CourseRepository courseRepository;
    private final PathRepository pathRepository;
    private final ApplicationConfig applicationConfig;
    private final JavaMailSender javaMailSender;

    @Override
    public void changeAccountDetails(Long accountId, ChangeAccountDetailsRequest request) {
        AccountDetails account = accountDetailsRepository.findById(accountId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[User Info Editing Error] No account details "
                        + "with id " + accountId + " found."));
        account.setName(ObjectUtils.isEmpty(request.getName()) ? account.getName() : request.getName());
        account.setDepartment(ObjectUtils.isEmpty(request.getDepartment())
                ? account.getDepartment() : request.getDepartment());
        account.setSeniority(ObjectUtils.isEmpty(request.getSeniority())
                ? account.getSeniority() : request.getSeniority());
        account.setPosition(ObjectUtils.isEmpty(request.getPosition())
                ? account.getPosition() : request.getPosition());

        try {
            accountDetailsRepository.save(account);
        } catch (DataAccessException e) {
            throw new AccountDetailsException("[User Info Editing Error] Account details with id " + accountId
                    + " could not be updated.");
        }
    }

    @Override
    public void createCourse(CreateCourseRequest request) {
        Course course = Course.builder()
                .name(request.getName())
                .category(request.getCategory())
                .maxEnrollments(request.getMaxEnrollments())
                .selfEnrollment(request.getSelfEnrollment())
                .completions(0L)
                .unEnrollments(0L)
                .currentEnrollments(0L)
                .numberOfAssignments(0)
                .courseCompletionStats(new ArrayList<>())
                .assignments(new ArrayList<>())
                .paths(new ArrayList<>())
                .build();

        List<AccountDetails> trainerDetails = accountDetailsRepository.findAllById(request.getTrainerIds());
        trainerDetails.forEach(trainerDetail -> trainerDetail.getTaughtCourses().add(course));
        course.setTrainersDetails(trainerDetails);
        try {
            courseRepository.save(course);
        } catch (DataAccessException e) {
            throw new CourseUniqueNameException("[Course Creation Error] Course could not be created.");
        }
    }

    @Override
    public List<TrainersToHRResponse> showTrainers(
            String name, String department, String position, String seniority,
            String email, Integer pageNo) {

        return accountDetailsRepository.findAll(PagingUtils.getPaging(pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(account -> account.getAccount().getRole().equals(Role.TRAINER))
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
                .map(account -> TrainersToHRResponse.builder()
                        .id(account.getId())
                        .email(account.getAccount().getEmail())
                        .name(account.getName())
                        .noTaughtCourses(getNoTaughtCourses(account))
                        .noCurrentTrainees(getTotalNoOfTraineesForATrainer(account))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void editCourse(Long courseId, EditCourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Course Editing Error] No course with id " + courseId
                        + " found."));
        course.setName(ObjectUtils.isEmpty(request.getName()) ? course.getName() : request.getName());
        course.setCategory(ObjectUtils.isEmpty(request.getCategory()) ? course.getCategory() : request.getCategory());
        course.setDescription(ObjectUtils.isEmpty(request.getDescription())
                ? course.getDescription() : request.getDescription());
        course.setSelfEnrollment(ObjectUtils.isEmpty(request.getSelfEnrollment())
                ? course.getSelfEnrollment() : request.getSelfEnrollment());
        course.setMaxEnrollments(ObjectUtils.isEmpty(request.getMaxEnrollments())
                ? course.getMaxEnrollments() : request.getMaxEnrollments());

        try {
            courseRepository.save(course);
        } catch (DataAccessException e) {
            throw new CourseUniqueNameException("[Course Editing Error] Course with id " + courseId
                    + " could not updated.");
        }
    }

    @Override
    public List<CoursesToHRResponse> showCourses(
            String name, String category, String sortBy,
            String sortMode, Integer pageNo) {

        return courseRepository.findAll(PagingUtils.getPaging(sortBy, sortMode, pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(course -> (ObjectUtils.isEmpty(name) || course.getName()
                        .toUpperCase(Locale.ROOT).contains(name.toUpperCase(Locale.ROOT))))
                .filter(course -> (ObjectUtils.isEmpty(category) || course.getCategory()
                        .toUpperCase(Locale.ROOT).contains(category.toUpperCase(Locale.ROOT))))
                .map(course -> CoursesToHRResponse.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .category(course.getCategory())
                        .canEnroll(course.getNumberOfAssignments() != 0 && !course.getSelfEnrollment())
                        .maxEnrollments(course.getMaxEnrollments())
                        .currentEnrollments(course.getCurrentEnrollments())
                        .noAssignments(course.getNumberOfAssignments())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<TrainersReportResponse> showTrainersReport(
            String name, String department, String position, String seniority, List<String> courses,
            Long currentTraineesLow, Long currentTraineesHigh, Integer currentNoCoursesLow,
            Integer currentNoCoursesHigh, List<String> paths, Integer currentNoPathsLow,
            Integer currentNoPathsHigh, Integer pageNo) {

        return accountDetailsRepository.findAll(PagingUtils.getPaging(pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(account -> account.getAccount().getRole().equals(Role.TRAINER))
                .filter(account -> (ObjectUtils.isEmpty(name) || account.getName()
                        .toUpperCase(Locale.ROOT).contains(name.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(department) || account.getDepartment()
                        .toUpperCase(Locale.ROOT).contains(department.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(seniority) || account.getSeniority()
                        .toUpperCase(Locale.ROOT).contains(seniority.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(position) || account.getPosition()
                        .toUpperCase(Locale.ROOT).contains(position.toUpperCase(Locale.ROOT))))
                .filter(account -> (CollectionUtils.isEmpty(courses)
                        || account.getTaughtCourses().stream().map(Course::getName).toList().containsAll(courses)))
                .filter(account -> (CollectionUtils.isEmpty(paths)
                        || account.getCreatedPaths().stream().map(Path::getName).toList().containsAll(paths)))
                .filter(account -> CheckIntervalUtils.checkNumberInInterval(
                        currentTraineesLow,
                        currentTraineesHigh,
                        getTotalNoOfTraineesForATrainer(account)))
                .filter(account -> CheckIntervalUtils.checkNumberInInterval(
                        currentNoCoursesLow,
                        currentNoCoursesHigh,
                        getNoTaughtCourses(account)))
                .filter(account -> CheckIntervalUtils.checkNumberInInterval(
                        currentNoPathsLow,
                        currentNoPathsHigh,
                        getNoCreatedPaths(account)))
                .map(account -> TrainersReportResponse.builder()
                        .name(account.getName())
                        .department(account.getDepartment())
                        .position(account.getPosition())
                        .seniority(account.getSeniority())
                        .noTaughtCourses(CollectionUtils.isEmpty(account.getTaughtCourses()) ?
                                0 : account.getTaughtCourses().size())
                        .noCreatedPaths(CollectionUtils.isEmpty(account.getCreatedPaths()) ?
                                0 : account.getCreatedPaths().size())
                        .noCurrentTrainees(getTotalNoOfTraineesForATrainer(account))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void createTrainersReport(Long hrId, CreateTrainersReportRequest request) {
        AccountDetails hrAccountDetails = accountDetailsRepository.findById(hrId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[User Info Editing Error] No account details "
                        + "with id " + hrId + " found."));

        List<List<String>> dataLines = new ArrayList<>();
        request.getReportList().forEach(response -> {
            List<String> line = new ArrayList<>();
            line.add(response.getName());
            line.add(ObjectUtils.isEmpty(response.getDepartment()) ? "-" : response.getDepartment());
            line.add(ObjectUtils.isEmpty(response.getPosition()) ? "-" : response.getPosition());
            line.add(ObjectUtils.isEmpty(response.getSeniority()) ? "-" : response.getSeniority());
            line.add(response.getNoTaughtCourses().toString());
            line.add(response.getNoCurrentTrainees().toString());
            line.add(response.getNoCreatedPaths().toString());
            dataLines.add(line);
        });

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setSubject("[Report] Trainers Report");
            helper.setFrom(applicationConfig.getEmailSenderAddress());
            helper.setTo(hrAccountDetails.getAccount().getEmail());
            helper.setText("Here is your generated <b>trainers</b> report.", true);
            FileSystemResource file = new FileSystemResource(CSVUtils.createCSVFile(dataLines,
                    applicationConfig.getTemporaryFileName()));
            helper.addAttachment("TrainersReport.csv", file);
        } catch (MessagingException e) {
            throw new ReportAttachmentException("[Trainer Report Creation Error] Attachment build failed.");
        }
        javaMailSender.send(message);

        try {
            Files.deleteIfExists(Paths.get(applicationConfig.getTemporaryFileName()));
        } catch (IOException e) {
            throw new ReportRemoveTemporaryFileException("[Trainer Report Creation Error] Removal of the temporary " +
                    "trainer report failed.");
        }
    }

    @Override
    public List<CoursesReportResponse> showCoursesReport(
            String name, String category, String selfEnrollment, List<String> trainers, List<String> paths,
            Integer assignmentsNoLow, Integer assignmentsNoHigh, Long completionsNoLow, Long completionsNoHigh,
            Long unEnrollmentsNoLow, Long unEnrollmentsNoHigh, Long currentEnrollmentsNoLow,
            Long currentEnrollmentsNoHigh, Long possibleEnrollmentsNoLow, Long possibleEnrollmentsNoHigh,
            Float completionRateLow, Float completionRateHigh, Float dropOutRateLow, Float dropOutRateHigh,
            String sortBy, String sortMode, Integer pageNo) {

        return courseRepository.findAll(PagingUtils.getPaging(sortBy, sortMode, pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(course -> (ObjectUtils.isEmpty(name) || course.getName()
                        .toUpperCase(Locale.ROOT).contains(name.toUpperCase(Locale.ROOT))))
                .filter(course -> (ObjectUtils.isEmpty(category) || course.getCategory()
                        .toUpperCase(Locale.ROOT).contains(category.toUpperCase(Locale.ROOT))))
                .filter(course -> (CollectionUtils.isEmpty(trainers)
                        || course.getTrainersDetails().stream().map(account -> account.getAccount().getEmail())
                        .toList().containsAll(trainers)))
                .filter(course -> (CollectionUtils.isEmpty(paths)
                        || course.getPaths().stream().map(Path::getName).toList().containsAll(paths)))
                .filter(course -> CheckIntervalUtils.checkNumberInInterval(
                        completionsNoLow,
                        completionsNoHigh,
                        course.getCompletions()))
                .filter(course -> CheckIntervalUtils.checkNumberInInterval(
                        unEnrollmentsNoLow,
                        unEnrollmentsNoHigh,
                        course.getUnEnrollments()))
                .filter(course -> CheckIntervalUtils.checkNumberInInterval(
                        currentEnrollmentsNoLow,
                        currentEnrollmentsNoHigh,
                        course.getCurrentEnrollments()))
                .filter(course -> CheckIntervalUtils.checkNumberInInterval(
                        possibleEnrollmentsNoLow,
                        possibleEnrollmentsNoHigh,
                        course.getMaxEnrollments()))
                .filter(course -> CheckIntervalUtils.checkNumberInInterval(
                        assignmentsNoLow,
                        assignmentsNoHigh,
                        course.getNumberOfAssignments()))
                .filter(course -> CheckIntervalUtils.checkNumberInInterval(
                        completionRateLow,
                        completionRateHigh,
                        getCourseCompletionRate(course)))
                .filter(course -> CheckIntervalUtils.checkNumberInInterval(
                        dropOutRateLow,
                        dropOutRateHigh,
                        getCourseDropOutRate(course)))
                .filter(course -> (selfEnrollment.equals("any")
                        || (course.getSelfEnrollment().toString().equals(selfEnrollment))))
                .map(course -> CoursesReportResponse.builder()
                        .name(course.getName())
                        .category(course.getCategory())
                        .selfEnrollment(course.getSelfEnrollment())
                        .noAssignments(course.getNumberOfAssignments())
                        .currentEnrollments(course.getCurrentEnrollments())
                        .maxEnrollments(course.getMaxEnrollments())
                        .completions(course.getCompletions())
                        .unEnrollments(course.getUnEnrollments())
                        .completionRate(getCourseCompletionRate(course))
                        .dropOutRate(getCourseDropOutRate(course))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void createCoursesReport(Long hrId, CreateCoursesReportRequest request) {
        AccountDetails hrAccountDetails = accountDetailsRepository.findById(hrId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[User Info Editing Error] No account details "
                        + "with id " + hrId + " found."));

        List<List<String>> dataLines = new ArrayList<>();
        request.getReportList().forEach(response -> {
            List<String> line = new ArrayList<>();
            line.add(response.getName());
            line.add(response.getCategory());
            line.add(response.getSelfEnrollment().toString());
            line.add(response.getNoAssignments().toString());
            line.add(response.getCurrentEnrollments().toString());
            line.add(response.getMaxEnrollments().toString());
            line.add(response.getCompletions().toString());
            line.add(response.getUnEnrollments().toString());
            line.add(response.getCompletionRate().toString());
            line.add(response.getDropOutRate().toString());
            dataLines.add(line);
        });

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setSubject("[Report] Courses Report");
            helper.setFrom(applicationConfig.getEmailSenderAddress());
            helper.setTo(hrAccountDetails.getAccount().getEmail());
            helper.setText("Here is your generated <b>courses</b> report.", true);
            FileSystemResource file = new FileSystemResource(CSVUtils.createCSVFile(dataLines,
                    applicationConfig.getTemporaryFileName()));
            helper.addAttachment("CoursesReport.csv", file);
        } catch (MessagingException e) {
            throw new ReportAttachmentException("[Course Report Creation Error] Attachment build failed.");
        }
        javaMailSender.send(message);

        try {
            Files.deleteIfExists(Paths.get(applicationConfig.getTemporaryFileName()));
        } catch (IOException e) {
            throw new ReportRemoveTemporaryFileException("[Course Report Creation Error] Removal of the temporary " +
                    "course report failed.");
        }
    }

    @Override
    public void deleteCourse(Long courseId) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Course Deletion Error] No course with id "
                        + courseId + " found."));
        course.getTrainersDetails().forEach(trainer -> trainer.getTaughtCourses().remove(course));
        course.getPaths().forEach(path -> path.getPathCompletionStats().forEach(
                stats -> {
                    if (stats.getCompletedCourses() != 0) {
                        stats.setCompletedCourses(stats.getCompletedCourses() - 1);
                    }
                    if (stats.getCompletedCourses().equals(path.getNumberOfCourses())) {
                        stats.setCompletionStatus(Boolean.TRUE);
                    }
                }
        ));
        course.getPaths().forEach(path -> {
            path.setNumberOfCourses(path.getNumberOfCourses() - 1);
            path.getCourses().remove(course);
        });

        try {
            courseRepository.delete(course);
        } catch (DataAccessException e) {
            throw new CourseDeletionException("[Course Deletion Error] Course with id " + courseId
                    + " could not be deleted.");
        }
    }

    @Override
    public List<EnrollmentsToHRResponse> showEnrollments(
            String name, String email, String department, String position,
            String seniority, Long courseId, Integer pageNo) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Course Enrollments Error] No course with id "
                        + courseId + " found."));

        return courseStatsRepository.findByCourse(course, PagingUtils.getPaging(pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(stats -> stats.getCompletionStatus().equals(Boolean.FALSE))
                .filter(stats -> (ObjectUtils.isEmpty(name) || stats.getAccountDetails().getName()
                        .toUpperCase(Locale.ROOT).contains(name.toUpperCase(Locale.ROOT))))
                .filter(stats -> (ObjectUtils.isEmpty(email) || stats.getAccountDetails().getAccount().getEmail()
                        .toUpperCase(Locale.ROOT).contains(email.toUpperCase(Locale.ROOT))))
                .filter(stats -> (ObjectUtils.isEmpty(department) || stats.getAccountDetails().getDepartment()
                        .toUpperCase(Locale.ROOT).contains(department.toUpperCase(Locale.ROOT))))
                .filter(stats -> (ObjectUtils.isEmpty(seniority) || stats.getAccountDetails().getSeniority()
                        .toUpperCase(Locale.ROOT).contains(seniority.toUpperCase(Locale.ROOT))))
                .filter(stats -> (ObjectUtils.isEmpty(position) || stats.getAccountDetails().getPosition()
                        .toUpperCase(Locale.ROOT).contains(position.toUpperCase(Locale.ROOT))))
                .map(stats -> EnrollmentsToHRResponse.builder()
                        .name(stats.getAccountDetails().getName())
                        .email(stats.getAccountDetails().getAccount().getEmail())
                        .enrollmentDate(stats.getEnrollmentDate().toString())
                        .build())
                .toList();
    }

    @Override
    public List<EnrollmentsToManageResponse> showEnrollmentsForManagement(
            String name, String email, String department, String position,
            String seniority, Long courseId, Integer pageNo) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Course Enrollments Management Error] No course with id "
                + courseId + " found."));

        return accountDetailsRepository.findAll(PagingUtils.getPaging(pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(account -> account.getAccount().getRole().equals(Role.TRAINEE))
                .filter(account -> determineEligibilityForManagement(course, account))
                .filter(account -> (ObjectUtils.isEmpty(name) || account.getName().toUpperCase(Locale.ROOT)
                        .contains(name.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(email) || account.getAccount().getEmail().toUpperCase(Locale.ROOT)
                        .contains(email.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(department) || account.getDepartment().toUpperCase(Locale.ROOT)
                        .contains(department.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(seniority) || account.getSeniority().toUpperCase(Locale.ROOT)
                        .contains(seniority.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(position) || account.getPosition().toUpperCase(Locale.ROOT)
                        .contains(position.toUpperCase(Locale.ROOT))))
                .map(account -> EnrollmentsToManageResponse.builder()
                        .enrollmentDate(getEnrollmentDateIfTraineeIsEnrolledOrNullOtherwise(account, courseId))
                        .id(account.getId())
                        .email(account.getAccount().getEmail())
                        .name(account.getName())
                        .build())
                .toList();
    }

    @Override
    public CourseEnrollmentDetailsResponse getCourseEnrollmentDetails(Long courseId) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("[Course Enrollments Details Error] No course with id "
                        + courseId + " found."));
        return CourseEnrollmentDetailsResponse.builder()
                .currentEnrollments(course.getCurrentEnrollments())
                .maxEnrollments(course.getMaxEnrollments())
                .build();
    }

    @Override
    public List<TraineesReportResponse> showTraineesReport(
            String name, String department, String position, String seniority, String course,
            Float progressLevelLow, Float progressLevelHigh, String enrollmentDateEarliest,
            String enrollmentDateLatest, String path, Float progressPathLevelLow,
            Float progressPathLevelHigh, Integer pageNo) {

        return accountDetailsRepository.findAll(PagingUtils.getPaging(pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(account -> account.getAccount().getRole().equals(Role.TRAINEE))
                .filter(account -> (ObjectUtils.isEmpty(name) || account.getName()
                        .toUpperCase(Locale.ROOT).contains(name.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(department) || account.getDepartment()
                        .toUpperCase(Locale.ROOT).contains(department.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(seniority) || account.getSeniority()
                        .toUpperCase(Locale.ROOT).contains(seniority.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(position) || account.getPosition()
                        .toUpperCase(Locale.ROOT).contains(position.toUpperCase(Locale.ROOT))))
                .filter(account -> (ObjectUtils.isEmpty(course) || checkIfTraineeIsEnrolled(account, course)))
                .filter(account -> (ObjectUtils.isEmpty(course) || (checkIfTraineeIsEnrolled(account, course)
                        && CheckIntervalUtils.checkNumberInInterval(progressLevelLow, progressLevelHigh,
                        determineTraineeProgressLevel(account, course)))))
                .filter(account -> (ObjectUtils.isEmpty(course) || (checkIfTraineeIsEnrolled(account, course)
                        && CheckIntervalUtils.checkDateInInterval(enrollmentDateEarliest, enrollmentDateLatest,
                        getEnrollmentDateIfTraineeIsEnrolled(account, course), applicationConfig.getDateFormat()))))
                .filter(account -> (ObjectUtils.isEmpty(path) || checkIfTraineeIsEnrolledPath(account, path)))
                .filter(account -> (ObjectUtils.isEmpty(path) || (checkIfTraineeIsEnrolledPath(account, path)
                        && CheckIntervalUtils.checkNumberInInterval(progressPathLevelLow, progressPathLevelHigh,
                        determineTraineePathProgressLevel(account, path)))))
                .map(account -> TraineesReportResponse.builder()
                        .name(account.getName())
                        .department(account.getDepartment())
                        .position(account.getPosition())
                        .seniority(account.getSeniority())
                        .progressLevel(checkIfTraineeIsEnrolled(account, course)
                                ? determineTraineeProgressLevel(account, course) : null)
                        .enrollmentDate(checkIfTraineeIsEnrolled(account, course)
                                ? new SimpleDateFormat(applicationConfig.getDateFormat())
                                .format(getEnrollmentDateIfTraineeIsEnrolled(account, course)) : null)
                        .pathProgressLevel(checkIfTraineeIsEnrolledPath(account, path)
                                ? determineTraineePathProgressLevel(account, path) : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void createTraineesReport(Long hrId, CreateTraineesReportRequest request) {
        AccountDetails hrAccountDetails = accountDetailsRepository.findById(hrId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[User Info Editing Error] No account details "
                        + "with id " + hrId + " found."));

        List<List<String>> dataLines = new ArrayList<>();
        request.getReportList().forEach(response -> {
            List<String> line = new ArrayList<>();
            line.add(response.getName());
            line.add(ObjectUtils.isEmpty(response.getDepartment()) ? "-" : response.getDepartment());
            line.add(ObjectUtils.isEmpty(response.getPosition()) ? "-" : response.getPosition());
            line.add(ObjectUtils.isEmpty(response.getSeniority()) ? "-" : response.getSeniority());
            line.add(response.getProgressLevel().toString());
            line.add(response.getEnrollmentDate());
            line.add(response.getPathProgressLevel().toString());
            dataLines.add(line);
        });

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setSubject("[Report] Trainees Report");
            helper.setFrom(applicationConfig.getEmailSenderAddress());
            helper.setTo(hrAccountDetails.getAccount().getEmail());
            helper.setText("Here is your generated <b>trainees</b> report.", true);
            FileSystemResource file = new FileSystemResource(CSVUtils.createCSVFile(dataLines,
                    applicationConfig.getTemporaryFileName()));
            helper.addAttachment("TraineesReport.csv", file);
        } catch (MessagingException e) {
            throw new ReportAttachmentException("[Trainee Report Creation Error] Attachment build failed.");
        }
        javaMailSender.send(message);

        try {
            Files.deleteIfExists(Paths.get(applicationConfig.getTemporaryFileName()));
        } catch (IOException e) {
            throw new ReportRemoveTemporaryFileException("[Trainee Report Creation Error] Removal of the temporary " +
                    "trainee report failed.");
        }
    }

    @Override
    public List<PathReportResponse> showPathsReport(
            String name, String category, String trainer, Integer courseNoLow, Integer courseNoHigh,
            Long completionsNoLow, Long completionsNoHigh, Long currentEnrollmentsNoLow, Long currentEnrollmentsNoHigh,
            List<String> courses, String sortBy, String sortMode, Integer pageNo) {

        return pathRepository.findAll(PagingUtils.getPaging(sortBy, sortMode, pageNo,
                applicationConfig.getFixedPageSize())).stream()
                .filter(path -> (ObjectUtils.isEmpty(name) || path.getName()
                        .toUpperCase(Locale.ROOT).contains(name.toUpperCase(Locale.ROOT))))
                .filter(path -> (ObjectUtils.isEmpty(category) || path.getCategory()
                        .toUpperCase(Locale.ROOT).contains(category.toUpperCase(Locale.ROOT))))
                .filter(path -> (ObjectUtils.isEmpty(trainer)
                        || (!ObjectUtils.isEmpty(path.getTrainerDetails())
                        && path.getTrainerDetails().getAccount().getEmail().equals(trainer))))
                .filter(path -> (CollectionUtils.isEmpty(courses)
                        || path.getCourses().stream().map(Course::getName).toList().containsAll(courses)))
                .filter(path -> CheckIntervalUtils.checkNumberInInterval(
                        completionsNoLow,
                        completionsNoHigh,
                        path.getCompletions()))
                .filter(path -> CheckIntervalUtils.checkNumberInInterval(
                        courseNoLow,
                        courseNoHigh,
                        path.getNumberOfCourses()))
                .filter(path -> CheckIntervalUtils.checkNumberInInterval(
                        currentEnrollmentsNoLow,
                        currentEnrollmentsNoHigh,
                        path.getCurrentEnrollments()))
                .map(path -> PathReportResponse.builder()
                        .name(path.getName())
                        .category(path.getCategory())
                        .noCourses(path.getNumberOfCourses())
                        .currentEnrollments(path.getCurrentEnrollments())
                        .completions(path.getCompletions())
                        .trainer(ObjectUtils.isEmpty(path.getTrainerDetails())
                                ? "-" : path.getTrainerDetails().getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void createPathsReport(Long hrId, CreatePathsReportRequest request) {
        AccountDetails hrAccountDetails = accountDetailsRepository.findById(hrId)
                .orElseThrow(() -> new AccountDetailsNotFoundException("[User Info Editing Error] No account details "
                        + "with id " + hrId + " found."));

        List<List<String>> dataLines = new ArrayList<>();
        request.getReportList().forEach(response -> {
            List<String> line = new ArrayList<>();
            line.add(response.getName());
            line.add(response.getCategory());
            line.add(response.getNoCourses().toString());
            line.add(response.getCurrentEnrollments().toString());
            line.add(response.getCompletions().toString());
            line.add(response.getTrainer());
            dataLines.add(line);
        });

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setSubject("[Report] Paths Report");
            helper.setFrom(applicationConfig.getEmailSenderAddress());
            helper.setTo(hrAccountDetails.getAccount().getEmail());
            helper.setText("Here is your generated <b>paths</b> report.", true);
            FileSystemResource file = new FileSystemResource(CSVUtils.createCSVFile(dataLines,
                    applicationConfig.getTemporaryFileName()));
            helper.addAttachment("PathsReport.csv", file);
        } catch (MessagingException e) {
            throw new ReportAttachmentException("[Path Report Creation Error] Attachment build failed.");
        }
        javaMailSender.send(message);

        try {
            Files.deleteIfExists(Paths.get(applicationConfig.getTemporaryFileName()));
        } catch (IOException e) {
            throw new ReportRemoveTemporaryFileException("[Path Report Creation Error] Removal of the temporary " +
                    "path report failed.");
        }
    }

    private Float getCourseDropOutRate(Course course) {
        if (course.getUnEnrollments() == 0) {
            return 0F;
        }
        return 100 - getCourseCompletionRate(course);
    }

    private Float getCourseCompletionRate(Course course) {
        final long totalRelevantEnrollments = course.getCompletions() + course.getUnEnrollments();

        if (totalRelevantEnrollments == 0) {
            return 0F;
        }
        return (float) (100 * (course.getCompletions() / totalRelevantEnrollments));
    }

    private int getNoTaughtCourses(AccountDetails account) {
        return CollectionUtils.isEmpty(account.getTaughtCourses()) ?
                0 : account.getTaughtCourses().size();
    }

    private int getNoCreatedPaths(AccountDetails account) {
        return CollectionUtils.isEmpty(account.getCreatedPaths()) ?
                0 : account.getCreatedPaths().size();
    }

    private Long getTotalNoOfTraineesForATrainer(AccountDetails trainer) {
        Long totalTrainees = 0L;
        if (!CollectionUtils.isEmpty(trainer.getTaughtCourses())) {
            for (Course course : trainer.getTaughtCourses()) {
                totalTrainees += course.getCurrentEnrollments();
            }
        }

        return totalTrainees;
    }

    private Boolean checkIfTraineeIsEnrolled(AccountDetails account, String courseName) {
        Optional<CourseCompletionStats> courseStats = account.getCourseCompletionStats().stream()
                .filter(stats -> stats.getCourse().getName().equals(courseName)).findAny();
        return courseStats.isPresent() ? Boolean.TRUE : Boolean.FALSE;
    }

    private boolean checkIfTraineeIsEnrolledPath(AccountDetails account, String pathName) {
        Optional<PathCompletionStats> pathStats = account.getPathCompletionStats().stream()
                .filter(stats -> stats.getPath().getName().equals(pathName)).findAny();
        return pathStats.isPresent() ? Boolean.TRUE : Boolean.FALSE;
    }

    private Timestamp getEnrollmentDateIfTraineeIsEnrolled(AccountDetails account, String courseName) {
        var courseStats = account.getCourseCompletionStats().stream()
                .filter(stats -> stats.getCourse().getName().equals(courseName)).findAny()
                .orElseThrow(() -> new CourseNotFoundException("[Trainees Report Error] No course with the name "
                        + courseName + " found."));

        return courseStats.getEnrollmentDate();
    }

    private String getEnrollmentDateIfTraineeIsEnrolledOrNullOtherwise(AccountDetails account, Long courseId) {
        Optional<CourseCompletionStats> courseStats = account.getCourseCompletionStats().stream()
                .filter(stats -> stats.getCourse().getId().equals(courseId))
                .findAny();

        return courseStats.map(courseCompletionStats
                -> courseCompletionStats.getEnrollmentDate().toString()).orElse(null);
    }

    private Float determineTraineeProgressLevel(AccountDetails account, String courseName) {
        var courseStats = account.getCourseCompletionStats().stream()
                .filter(stats -> stats.getCourse().getName().equals(courseName)).findAny()
                .orElseThrow(() -> new CourseNotFoundException("[Trainees Report Error] No course with the name "
                        + courseName + " found."));

        return (float) (100 * (courseStats.getCompletedAssignments()
                / courseStats.getCourse().getNumberOfAssignments()));
    }

    private Float determineTraineePathProgressLevel(AccountDetails account, String pathName) {
        var pathStats = account.getPathCompletionStats().stream()
                .filter(stats -> stats.getPath().getName().equals(pathName)).findAny()
                .orElseThrow(() -> new PathCompletionStatsNotFoundException("[Trainees Report Error] No path with the "
                        + "name " + pathName + " found."));

        return (float) (100 * (pathStats.getCompletedCourses() / pathStats.getPath().getNumberOfCourses()));
    }

    private boolean determineEligibilityForManagement(Course course, AccountDetails account) {
        var optionalStats = course.getCourseCompletionStats().stream()
                .filter(stats -> stats.getAccountDetails().getId().equals(account.getId()))
                .findAny();
        return optionalStats.isEmpty() || !optionalStats.get().getCompletionStatus().equals(Boolean.TRUE);
    }
}
