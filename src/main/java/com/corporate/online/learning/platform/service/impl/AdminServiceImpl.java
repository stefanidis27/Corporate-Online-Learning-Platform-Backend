package com.corporate.online.learning.platform.service.impl;

import com.corporate.online.learning.platform.exception.account.AccountDeletionException;
import com.corporate.online.learning.platform.exception.account.AccountNotFoundException;
import com.corporate.online.learning.platform.exception.course.CourseException;
import com.corporate.online.learning.platform.exception.path.PathException;
import com.corporate.online.learning.platform.repository.account.AccountRepository;
import com.corporate.online.learning.platform.repository.course.CourseRepository;
import com.corporate.online.learning.platform.repository.path.PathRepository;
import com.corporate.online.learning.platform.service.AdminService;
import com.corporate.online.learning.platform.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AccountRepository accountRepository;
    private final CourseRepository courseRepository;
    private final EmailService emailService;
    private final PathRepository pathRepository;

    @Override
    public void deleteAccount(Long accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("[Account Deletion Error] No account with id "
                        + accountId + " was found."));
        String email = account.getEmail();
        account.getAccountDetails().getTaughtCourses().forEach(course ->
                course.getTrainersDetails().remove(account.getAccountDetails()));
        account.getAccountDetails().getCreatedPaths().forEach(path ->
                path.setTrainerDetails(null));

        account.getAccountDetails().getCourseCompletionStats()
                .forEach(stats -> {
                    stats.getCourse().setCurrentEnrollments(stats.getCourse().getCurrentEnrollments() - 1);
                    try {
                        courseRepository.save(stats.getCourse());
                    } catch (DataAccessException e) {
                        throw new CourseException("[Account Deletion Error] Course with id " + stats.getCourse().getId()
                                + " could not be updated with fewer course completion stats.");
                    }
                });
        account.getAccountDetails().getPathCompletionStats()
                .forEach(stats -> {
                    stats.getPath().setCurrentEnrollments(stats.getPath().getCurrentEnrollments() - 1);
                    try {
                        pathRepository.save(stats.getPath());
                    } catch (DataAccessException e) {
                        throw new PathException("[Account Deletion Error] Path with id " + stats.getPath().getId()
                                + " could not be updated with fewer path completion stats.");
                    }
                });

        try {
            accountRepository.delete(account);
        } catch (DataAccessException e) {
            throw new AccountDeletionException("[Account Deletion Error] Account with id "
                    + accountId + " could not be deleted.");
        }
        emailService.sendEmailAccountDeletionConfirmation(email);
    }
}
