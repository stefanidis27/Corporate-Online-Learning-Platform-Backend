package com.corporate.online.learning.platform.service;

import com.corporate.online.learning.platform.model.assignment.AssignmentCompletionStats;
import com.corporate.online.learning.platform.model.course.CourseCompletionStats;

import java.util.List;

public interface EmailService {

    void sendEmailResetPasswordConfirmation(String email, String newPassword);

    void sendEmailAccountCreationConfirmation(String email, String password);

    void sendEmailCredentialsChangeConfirmation(String oldEmail, String newEmail, String newPassword);

    void sendEmailAccountLockedConfirmation(String email);

    void sendEmailAccountDeletionConfirmation(String email);

    void sendEmailCourseCreationConfirmation(List<String> emailList, String courseName);

    void sendEmailReport(String email, List<List<String>> dataLines, String reportType);

    void sendEmailAccountDetailsChangeConfirmation(String email);

    void sendEmailRejectedAssignment(AssignmentCompletionStats assignmentStats);

    void sendEmailCourseCompletedConfirmation(CourseCompletionStats courseStats);

    void sendEmailApprovedAssignmentConfirmation(AssignmentCompletionStats courseStats);

    void sendEmailCourseEnrollmentConfirmation(String courseName, String email);

    void sendEmailCourseUnEnrollmentConfirmation(String courseName, String email);
}
