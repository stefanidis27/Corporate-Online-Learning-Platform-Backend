package com.corporate.online.learning.platform.controller;

import com.corporate.online.learning.platform.dto.request.hr.CreateCourseRequest;
import com.corporate.online.learning.platform.service.HRService;
import com.corporate.online.learning.platform.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HRControllerTest {

    @Mock
    private HRService hrService;

    @Captor
    private ArgumentCaptor<CreateCourseRequest> createCourseRequestCaptor;

    @InjectMocks
    private HRController underTest;

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
        doNothing().when(hrService).createCourse(createCourseRequestCaptor.capture());

        // When
        ResponseEntity<Void> actual = underTest.createCourse(request);
        CreateCourseRequest serviceRequest = createCourseRequestCaptor.getValue();

        // Then
        verify(hrService, times(1)).createCourse(serviceRequest);
        assertThat(serviceRequest).isEqualTo(request);
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
