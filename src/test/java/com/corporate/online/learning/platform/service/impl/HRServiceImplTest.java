package com.corporate.online.learning.platform.service.impl;

import com.corporate.online.learning.platform.dto.request.hr.CreateCourseRequest;
import com.corporate.online.learning.platform.exception.course.CourseUniqueNameException;
import com.corporate.online.learning.platform.model.account.Account;
import com.corporate.online.learning.platform.model.account.AccountDetails;
import com.corporate.online.learning.platform.model.course.Course;
import com.corporate.online.learning.platform.repository.account.AccountDetailsRepository;
import com.corporate.online.learning.platform.repository.course.CourseRepository;
import com.corporate.online.learning.platform.service.EmailService;
import com.corporate.online.learning.platform.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HRServiceImplTest {

    private static final String DUMMY_EMAIL = "dummy_email";
    private static final String COURSE_CREATION_EXCEPTION_MESSAGE
            = "[Course Creation Error] Course could not be created.";

    @Mock
    private AccountDetailsRepository accountDetailsRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<List<String>> stringListCaptor;
    @Captor
    private ArgumentCaptor<Course> courseCaptor;

    @InjectMocks
    private HRServiceImpl underTest;

    @BeforeAll
    public static void setUp() {
        Mockito.reset();
    }

    @Test
    public void shouldCreateCourse() {
        // Given
        CreateCourseRequest request = CreateCourseRequest.builder()
                .category(TestUtils.courseCategory)
                .name(TestUtils.courseName)
                .maxEnrollments(TestUtils.courseMaxEnrollments)
                .selfEnrollment(Boolean.TRUE)
                .trainerIds(Collections.singletonList(TestUtils.trainerId))
                .build();
        Account account = Account.builder().email(DUMMY_EMAIL).build();
        AccountDetails trainerDetails = AccountDetails.builder()
                .taughtCourses(new ArrayList<>())
                .account(account)
                .build();
        Course expected = Course.builder()
                .name(TestUtils.courseName)
                .category(TestUtils.courseCategory)
                .maxEnrollments(TestUtils.courseMaxEnrollments)
                .selfEnrollment(Boolean.TRUE)
                .completions(0L)
                .unEnrollments(0L)
                .currentEnrollments(0L)
                .numberOfAssignments(0)
                .courseCompletionStats(new ArrayList<>())
                .assignments(new ArrayList<>())
                .paths(new ArrayList<>())
                .trainersDetails(Collections.singletonList(trainerDetails))
                .build();

        when(accountDetailsRepository.findAllById(any())).thenReturn(Collections.singletonList(trainerDetails));
        when(courseRepository.save(courseCaptor.capture())).thenReturn(null);
        doNothing().when(emailService).sendEmailCourseCreationConfirmation(stringListCaptor.capture(), any());

        // When
        underTest.createCourse(request);

        // Then
        verify(accountDetailsRepository, times(1)).findAllById(any());
        verify(courseRepository, times(1)).save(any());
        verify(emailService, times(1)).sendEmailCourseCreationConfirmation(any(), any());
        assertThat(stringListCaptor.getValue()).isEqualTo(Collections.singletonList(DUMMY_EMAIL));
        assertThat(courseCaptor.getValue()).isEqualTo(expected);
    }

    @Test
    public void shouldNotCreateCourseGivenCourseRepositoryException() {
        // Given
        CreateCourseRequest request = CreateCourseRequest.builder()
                .category(TestUtils.courseCategory)
                .name(TestUtils.courseName)
                .maxEnrollments(TestUtils.courseMaxEnrollments)
                .selfEnrollment(Boolean.TRUE)
                .trainerIds(Collections.singletonList(TestUtils.trainerId))
                .build();
        Account account = Account.builder().email(DUMMY_EMAIL).build();
        AccountDetails trainerDetails = AccountDetails.builder()
                .taughtCourses(new ArrayList<>())
                .account(account)
                .build();

        when(accountDetailsRepository.findAllById(any())).thenReturn(Collections.singletonList(trainerDetails));
        when(courseRepository.save(any())).thenThrow(new CourseUniqueNameException(COURSE_CREATION_EXCEPTION_MESSAGE));

        // When
        try {
            underTest.createCourse(request);
            fail(COURSE_CREATION_EXCEPTION_MESSAGE);
        } catch (CourseUniqueNameException courseUniqueNameException) {
            assertThat(courseUniqueNameException.getMessage()).isEqualTo(COURSE_CREATION_EXCEPTION_MESSAGE);
        }

        // Then
        verify(accountDetailsRepository, times(1)).findAllById(any());
        verify(courseRepository, times(1)).save(any());
    }
}
