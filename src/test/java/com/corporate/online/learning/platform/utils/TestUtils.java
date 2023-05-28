package com.corporate.online.learning.platform.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:application.properties")
public class TestUtils {

    @Value("${course.category}")
    public static String courseCategory;

    @Value("${course.name}")
    public static String courseName;

    @Value("${course.max.enrollments}")
    public static long courseMaxEnrollments;

    @Value("${trainer.id}")
    public static long trainerId;
}
