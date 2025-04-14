package com.dementor.domain.job.controller;

import com.dementor.domain.job.dto.request.JobCreaeteRequest;
import com.dementor.domain.job.dto.request.JobUpdateRequest;
import com.dementor.domain.job.dto.response.JobCreateResponse;
import com.dementor.domain.job.dto.response.JobFindResponse;
import com.dementor.domain.job.dto.response.JobUpdateResponse;
import com.dementor.domain.job.service.JobService;
import com.dementor.global.ApiResponse;
import com.dementor.global.common.swaggerDocs.JobClassSwagger;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/job")
@RequiredArgsConstructor
@Tag(name = "직무 API", description = "멘토 지원, 정보 수정, 조회 API")
public class JobController implements JobClassSwagger {
	private final JobService jobService;

	@GetMapping
	public ResponseEntity<ApiResponse<List<JobFindResponse>>> getJobList() {
		List<JobFindResponse> list = jobService.getJobList();
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(
				true,
				HttpStatus.OK,
				"직무 전체 조회 성공",
				list
			));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<JobCreateResponse>> createJob(
		@RequestBody JobCreaeteRequest request
	) {
		JobCreateResponse response = jobService.createJob(request);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.of(
				true,
				HttpStatus.CREATED,
				"직무 생성 성공",
				response
			));
	}

	@PutMapping("/{jobId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<JobUpdateResponse>> updateJob(
		@PathVariable Long jobId,
		@RequestBody JobUpdateRequest request
	) {
		JobUpdateResponse response = jobService.updateJob(jobId, request);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(
				true,
				HttpStatus.OK,
				"직무 정보 수정 성공",
				response
			));
	}

	@DeleteMapping("/{jobId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<?> deleteJob(
		@PathVariable Long jobId
	) {
		jobService.deleteJob(jobId);
		return ApiResponse.of(
			true,
			HttpStatus.OK,
			"직무 삭제 성공",
			null
		);
	}
}
