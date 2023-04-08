package com.corporate.online.learning.platform.dto.request.hr;

import com.corporate.online.learning.platform.dto.response.hr.TraineesReportResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTraineesReportRequest {

    private List<TraineesReportResponse> reportList;
}
