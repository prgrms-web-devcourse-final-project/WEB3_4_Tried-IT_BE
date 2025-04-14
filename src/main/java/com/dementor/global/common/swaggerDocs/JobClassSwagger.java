package com.dementor.global.common.swaggerDocs;

import com.dementor.domain.job.dto.request.JobCreaeteRequest;
import com.dementor.domain.job.dto.request.JobUpdateRequest;
import com.dementor.domain.job.dto.response.JobCreateResponse;
import com.dementor.domain.job.dto.response.JobFindResponse;
import com.dementor.domain.job.dto.response.JobUpdateResponse;
import com.dementor.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "직무 관리", description = "직무 데이터 관리")
public interface JobClassSwagger {

    @Operation(summary = "직무 전체 조회", description = "모든 직무 데이터를 조회합니다.")
    ResponseEntity<ApiResponse<List<JobFindResponse>>> getJobList();

    @Operation(summary = "직무 생성", description = "직무 데이터를 생성합니다.")
    ResponseEntity<ApiResponse<JobCreateResponse>> createJob(@RequestBody JobCreaeteRequest request);

    @Operation(summary = "직무 수정", description = "직무 데이터를 수정합니다.")
    ResponseEntity<ApiResponse<JobUpdateResponse>> updateJob(@PathVariable Long jobId, @RequestBody JobUpdateRequest request);

    @Operation(summary = "직무 삭제", description = "직무 데이터를 삭제합니다.")
    ApiResponse<?> deleteJob(@PathVariable Long jobId);
}
