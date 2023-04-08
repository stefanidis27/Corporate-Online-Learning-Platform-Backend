package com.corporate.online.learning.platform.exception;

import com.corporate.online.learning.platform.exception.account.*;
import com.corporate.online.learning.platform.exception.assignment.AssignmentCompletionStatsDeletionException;
import com.corporate.online.learning.platform.exception.assignment.AssignmentCompletionStatsException;
import com.corporate.online.learning.platform.exception.assignment.AssignmentCompletionStatsNotFoundException;
import com.corporate.online.learning.platform.exception.course.*;
import com.corporate.online.learning.platform.exception.path.PathCompletionStatsException;
import com.corporate.online.learning.platform.exception.path.PathCompletionStatsNotFoundException;
import com.corporate.online.learning.platform.exception.path.PathException;
import com.corporate.online.learning.platform.exception.path.PathNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(AccountNotFoundException.class)
    public final ResponseEntity<ErrorMessage> handleAccountNotFoundException(AccountNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(AccountUniqueEmailException.class)
    public final ResponseEntity<ErrorMessage> handleAccountUniqueEmailException(AccountUniqueEmailException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(AccountException.class)
    public final ResponseEntity<ErrorMessage> handleAccountException(AccountException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(AccountDetailsNotFoundException.class)
    public final ResponseEntity<ErrorMessage> handleAccountDetailsNotFoundException(AccountDetailsNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(AccountDetailsException.class)
    public final ResponseEntity<ErrorMessage> handleAccountDetailsException(AccountDetailsException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public final ResponseEntity<ErrorMessage> handleTokenNotFoundException(TokenNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(TokenException.class)
    public final ResponseEntity<ErrorMessage> handleTokenException(TokenException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(PathNotFoundException.class)
    public final ResponseEntity<ErrorMessage> handlePathNotFoundException(PathNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(PathException.class)
    public final ResponseEntity<ErrorMessage> handlePathException(PathException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(PathCompletionStatsNotFoundException.class)
    public final ResponseEntity<ErrorMessage> handlePathCompletionStatsNotFoundException(
            PathCompletionStatsNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(e.getMessage()));
    }


    @ExceptionHandler(PathCompletionStatsException.class)
    public final ResponseEntity<ErrorMessage> handlePathCompletionStatsException(PathCompletionStatsException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public final ResponseEntity<ErrorMessage> handleCourseNotFoundException(CourseNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(CourseException.class)
    public final ResponseEntity<ErrorMessage> handleCourseException(CourseException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(CourseCompletionStatsNotFoundException.class)
    public final ResponseEntity<ErrorMessage> handleCourseCompletionStatsNotFoundException(
            CourseCompletionStatsNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(CourseCompletionStatsException.class)
    public final ResponseEntity<ErrorMessage> handleCourseCompletionStatsException(CourseCompletionStatsException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(CourseCompletionStatsDeletionException.class)
    public final ResponseEntity<ErrorMessage> handleCourseCompletionStatsDeletionException(
            CourseCompletionStatsDeletionException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(AssignmentCompletionStatsNotFoundException.class)
    public final ResponseEntity<ErrorMessage> handleAssignmentCompletionStatsNotFoundException(
            AssignmentCompletionStatsNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(AssignmentCompletionStatsException.class)
    public final ResponseEntity<ErrorMessage> handleAssignmentCompletionStatsException(
            AssignmentCompletionStatsException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(AssignmentCompletionStatsDeletionException.class)
    public final ResponseEntity<ErrorMessage> handleAssignmentCompletionStatsDeletionException(
            AssignmentCompletionStatsDeletionException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }
}
