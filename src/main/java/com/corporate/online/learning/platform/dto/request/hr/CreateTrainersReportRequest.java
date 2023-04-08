package com.corporate.online.learning.platform.dto.request.hr;

import com.corporate.online.learning.platform.dto.response.hr.TrainersReportResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainersReportRequest {

    private List<TrainersReportResponse> reportList;
}
