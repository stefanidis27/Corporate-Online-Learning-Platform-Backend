package com.corporate.online.learning.platform.service;

import java.util.List;

public interface EmailService {

    void sendEmailResetPasswordConfirmation(String email, String newPassword);

    void sendEmailAccountCreationConfirmation(String email, String password);

    void sendEmailCredentialsChangeConfirmation(String oldEmail, String newEmail, String newPassword);

    void sendEmailAccountLockedConfirmation(String email);

    void sendEmailAccountDeletionConfirmation(String email);

    void sendEmailCourseCreationConfirmation(List<String> emailList, String courseName);

    void sendEmailReport(String email, List<List<String>> dataLines, String reportType);
}
