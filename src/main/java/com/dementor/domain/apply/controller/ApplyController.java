package com.dementor.domain.apply.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dementor.domain.apply.dto.request.ApplyCreateRequest;
import com.dementor.domain.apply.dto.response.ApplyIdResponse;
import com.dementor.domain.apply.dto.response.ApplyPageResponse;
import com.dementor.domain.apply.dto.response.ApplyScheduleResponse;
import com.dementor.domain.apply.service.ApplyService;
import com.dementor.global.ApiResponse;
import com.dementor.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "멘토링 신청", description = "멘토링 신청 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/apply")
public class ApplyController {

	private final ApplyService applyService;


	@Operation(summary = "멘토링 신청", description = "멘토링을 신청합니다")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ApplyIdResponse>  createApply(
		@RequestBody ApplyCreateRequest req,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
			ApplyIdResponse response = applyService.createApply(req, userDetails.getId());

			return ApiResponse.of(true, HttpStatus.CREATED, "멘토링 신청이 완료되었습니다", response);
	}


	@Operation(summary = "멘토링 신청 취소", description = "멘토링 신청을 취소합니다")
	@DeleteMapping("/{applyId}")
	public ApiResponse<Void> deleteApply(
		@PathVariable(name = "applyId") Long applyId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
			applyService.deleteApply(applyId, userDetails.getId());

			return ApiResponse.of(true, HttpStatus.OK, "멘토링 신청이 취소되었습니다");

	}


	@Operation(summary = "멘토링 신청 목록 조회", description = "내가 신청한 멘토링 목록을 조회합니다")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ApplyPageResponse> getApply(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		ApplyPageResponse response = applyService.getApplyList(userDetails.getId(), page-1, size);
		return ApiResponse.of(true, HttpStatus.OK, "멘토링 신청 목록을 조회했습니다", response);
	}

	// 특정 멘토링 신청 날짜 목록 조회
	@Operation(summary = "멘토링 신청 날짜 목록 조회", description = "특정 멘토링 클래스에 신청된 날짜 목록을 조회합니다")
	@GetMapping("/schedules/{classId}")
	public ApiResponse<ApplyScheduleResponse> getApplySchedules(
		@PathVariable Long classId,
		@Parameter(description = "시작 날짜", example = "20250408")
		@RequestParam("startDate") String startDate,
		@Parameter(description = "종료 날짜", example = "20250430")
		@RequestParam("endDate") String endDate) {

		ApplyScheduleResponse response = applyService.getApplySchedulesByClassId(classId, startDate, endDate);
		return ApiResponse.of(true, HttpStatus.OK, "멘토링 신청 날짜 목록을 조회했습니다", response);
	}
}
