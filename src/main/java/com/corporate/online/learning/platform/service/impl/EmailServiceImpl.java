package com.corporate.online.learning.platform.service.impl;

import com.corporate.online.learning.platform.config.ApplicationConfig;
import com.corporate.online.learning.platform.exception.report.ReportAttachmentException;
import com.corporate.online.learning.platform.exception.report.ReportRemoveTemporaryFileException;
import com.corporate.online.learning.platform.model.assignment.AssignmentCompletionStats;
import com.corporate.online.learning.platform.model.course.CourseCompletionStats;
import com.corporate.online.learning.platform.service.EmailService;
import com.corporate.online.learning.platform.utils.CSVUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final ApplicationConfig applicationConfig;

    @Override
    public void sendEmailResetPasswordConfirmation(String email, String newPassword) {
        sendEmailWithNoAttachment(
                applicationConfig.getEmailBodyResetPassword().replace("{placeholder}", newPassword),
                applicationConfig.getEmailSubjectResetPassword(),
                email);
    }

    @Override
    public void sendEmailAccountCreationConfirmation(String email, String password) {
        sendEmailWithNoAttachment(
                applicationConfig.getEmailBodyAccountCreation().replace("{placeholder}", password),
                applicationConfig.getEmailSubjectAccountCreation(),
                email);
    }

    @Override
    public void sendEmailCredentialsChangeConfirmation(String oldEmail, String newEmail, String newPassword) {
        String messageNewPassword = ObjectUtils.isEmpty(newPassword)
                ? "" : (ObjectUtils.isEmpty(newEmail)
                ? applicationConfig.getEmailBodyCredentialsChangePasswordMessage()
                : " " + applicationConfig.getEmailBodyCredentialsChangePasswordMessage());

        if (!ObjectUtils.isEmpty(newEmail)) {
            sendEmailWithNoAttachment(
                    applicationConfig.getEmailBodyCredentialsChange()
                            .replace("{email_placeholder}",
                                    applicationConfig.getEmailBodyCredentialsChangeOldEmailMessage()
                                            .replace("{placeholder}", newEmail))
                            .replace("{password_placeholder}", messageNewPassword),
                    applicationConfig.getEmailSubjectCredentialsChange(),
                    oldEmail);
            sendEmailWithNoAttachment(
                    applicationConfig.getEmailBodyCredentialsChange()
                            .replace("{email_placeholder}",
                                    applicationConfig.getEmailBodyCredentialsChangeNewEmailMessage())
                            .replace("{password_placeholder}", messageNewPassword),
                    applicationConfig.getEmailSubjectCredentialsChange(),
                    newEmail);
        } else {
            sendEmailWithNoAttachment(
                    applicationConfig.getEmailBodyCredentialsChange()
                            .replace("{email_placeholder}", "")
                            .replace("{password_placeholder}", messageNewPassword),
                    applicationConfig.getEmailSubjectCredentialsChange(),
                    oldEmail);
        }
    }

    @Override
    public void sendEmailAccountLockedConfirmation(String email) {
        sendEmailWithNoAttachment(
                applicationConfig.getEmailBodyAccountLocked().replace("{placeholder}",
                        applicationConfig.getAccountLockTime().toString()),
                applicationConfig.getEmailSubjectAccountLocked(),
                email);
    }

    @Override
    public void sendEmailAccountDeletionConfirmation(String email) {
        sendEmailWithNoAttachment(
                applicationConfig.getEmailBodyAccountDeleted(),
                applicationConfig.getEmailSubjectAccountDeleted(),
                email);
    }

    @Override
    public void sendEmailCourseCreationConfirmation(List<String> emailList, String courseName) {
        sendEmailWithNoAttachment(
                applicationConfig.getEmailBodyCourseCreated().replace("{placeholder}", courseName),
                applicationConfig.getEmailSubjectCourseCreated(),
                emailList.toArray(new String[0]));
    }

    @Override
    public void sendEmailReport(String email, List<List<String>> dataLines, String reportType) {
        String reportTypeLabel = reportType.charAt(0) + reportType.substring(1).toLowerCase(Locale.ROOT);
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);

            helper.setSubject("[Report] " + reportTypeLabel + " Report");
            helper.setFrom(applicationConfig.getEmailSenderAddress());
            helper.setTo(email);
            helper.setText("Here is your generated <b>" + reportType.toLowerCase(Locale.ROOT)
                    + "</b> report.", true);
            FileSystemResource file = new FileSystemResource(CSVUtils.createCSVFile(dataLines,
                    applicationConfig.getTemporaryFileName()));
            helper.addAttachment(reportType.toLowerCase(Locale.ROOT) + "_report.csv", file);
        } catch (MessagingException e) {
            throw new ReportAttachmentException("[" + reportTypeLabel
                    + " Report Creation Error] Attachment build failed.");
        }
        javaMailSender.send(message);

        try {
            Files.deleteIfExists(Paths.get(applicationConfig.getTemporaryFileName()));
        } catch (IOException e) {
            throw new ReportRemoveTemporaryFileException("[" + reportTypeLabel
                    + " Report Creation Error] Removal of the temporary report failed.");
        }
    }

    @Override
    public void sendEmailAccountDetailsChangeConfirmation(String email) {
        sendEmailWithNoAttachment(
                applicationConfig.getEmailBodyInfoChanged(),
                applicationConfig.getEmailSubjectInfoChanged(),
                email);
    }

    @Override
    public void sendEmailRejectedAssignment(AssignmentCompletionStats assignmentStats) {
        String fullComment = assignmentStats.getComment();
        String trainerComment = fullComment.substring(fullComment.indexOf(":") + 2);
        String trainerName = fullComment.substring(0, fullComment.indexOf(":"));
        String courseName = assignmentStats.getAssignment().getCourse().getName();
        String assignmentText = assignmentStats.getAssignment().getText();
        String email = assignmentStats.getAccountDetails().getAccount().getEmail();

        sendEmailWithNoAttachment(
                applicationConfig.getEmailBodyAssignmentRejected()
                        .replace("{placeholder_course}", courseName)
                        .replace("{placeholder_trainer}", trainerName)
                        .replace("{placeholder_comment}", trainerComment)
                        .replace("{placeholder_text}", assignmentText),
                applicationConfig.getEmailSubjectAssignmentRejected(),
                email);
    }

    @Override
    public void sendEmailCourseCompletedConfirmation(CourseCompletionStats courseStats) {
        String courseName = courseStats.getCourse().getName();
        String category = courseStats.getCourse().getCategory();
        String email = courseStats.getAccountDetails().getAccount().getEmail();

        sendEmailWithNoAttachment(
                applicationConfig.getEmailBodyCourseCompleted()
                        .replace("{placeholder_course}", courseName)
                        .replace("{placeholder_category}", category),
                applicationConfig.getEmailSubjectCourseCompleted(),
                email);
    }

    @Override
    public void sendEmailApprovedAssignmentConfirmation(AssignmentCompletionStats assignmentStats) {
        String courseName = assignmentStats.getAssignment().getCourse().getName();
        String assignmentText = assignmentStats.getAssignment().getText();
        String email = assignmentStats.getAccountDetails().getAccount().getEmail();

        sendEmailWithNoAttachment(
                applicationConfig.getEmailBodyAssignmentApproved()
                        .replace("{placeholder_course}", courseName)
                        .replace("{placeholder_text}", assignmentText),
                applicationConfig.getEmailSubjectAssignmentApproved(),
                email);
    }

    @Override
    public void sendEmailCourseEnrollmentConfirmation(String courseName, String email) {
        sendEmailWithNoAttachment(
                applicationConfig.getEmailBodyEnrolledInCourse().replace("{placeholder}", courseName),
                applicationConfig.getEmailSubjectEnrolledInCourse(),
                email);
    }

    @Override
    public void sendEmailCourseUnEnrollmentConfirmation(String courseName, String email) {
        sendEmailWithNoAttachment(
                applicationConfig.getEmailBodyUnEnrolledFromCourse().replace("{placeholder}", courseName),
                applicationConfig.getEmailSubjectUnEnrolledFromCourse(),
                email);
    }

    private void sendEmailWithNoAttachment(String emailBody, String emailSubject, String... email) {
        var mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(applicationConfig.getEmailSenderAddress());
        mailMessage.setTo(email);
        mailMessage.setText(emailBody);
        mailMessage.setSubject(emailSubject);

        javaMailSender.send(mailMessage);
    }
}
