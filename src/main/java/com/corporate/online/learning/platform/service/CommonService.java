package com.corporate.online.learning.platform.service;

import com.corporate.online.learning.platform.dto.response.common.AllAccountsResponse;
import com.corporate.online.learning.platform.dto.response.common.PersonalInfoResponse;
import com.corporate.online.learning.platform.dto.response.common.WebsiteInfoResponse;

import java.util.List;

public interface CommonService {

    PersonalInfoResponse getPersonalInfo(Long id);

    WebsiteInfoResponse getWebsiteDescription();

    List<AllAccountsResponse> showAllAccounts(
            String name, String department, String position, String seniority,
            String email, Long id, Integer pageNo);

    void completeAssignment(Long assignmentStatsId);

    void enrollInCourse(Long courseId, Long traineeId);

    void unEnrollFromCourse(Long courseId, Long traineeId);
}
