package com.dementor.domain.admin.controller;

import com.dementor.domain.mentorapplyproposal.dto.request.ApplymentRejectRequest;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentApprovalResponse;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentDetailResponse;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentRejectResponse;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentResponse;
import com.dementor.domain.mentorapplyproposal.service.AdminMentorApplymentService;
import com.dementor.global.ApiResponse;
import com.dementor.global.common.pagination.PaginationUtil;
import com.dementor.global.common.swaggerDocs.AdminMentorApplymentSwagger;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/mentor/applyment")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMentorApplymentController implements AdminMentorApplymentSwagger {

	private final AdminMentorApplymentService adminMentorApplymentService;

	@Override
	@GetMapping
	public ResponseEntity<ApiResponse<Page<ApplymentResponse>>> findAllAdminMentorApplyment(
		@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Pageable domainPageable = PaginationUtil.getApplymentPageable(pageable);

		Page<ApplymentResponse> responses = adminMentorApplymentService.findAllApplyment(domainPageable);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(
				true,
				HttpStatus.OK,
				"멘토 지원 전체 조회 성공",
				responses
			));
	}

	@Override
	@GetMapping("/{memberId}")
	public ResponseEntity<ApiResponse<ApplymentDetailResponse>> findOneApplyment(
		@PathVariable Long memberId
	) {
		ApplymentDetailResponse response = adminMentorApplymentService.findOneApplyment(memberId);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(
				true,
				HttpStatus.OK,
				"멘토 지원서 상세 조회 성공",
				response
			));
	}

	@Override
	@PostMapping("/{memberId}")
	public ResponseEntity<ApiResponse<ApplymentApprovalResponse>> approveApplyment(
		@PathVariable Long memberId
	) {
		ApplymentApprovalResponse response = adminMentorApplymentService.approveApplyment(memberId);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(
				true,
				HttpStatus.OK,
				"멘토 지원 승인",
				response
			));
	}

	@Override
	@PutMapping("/{memberId}/reject")
	public ResponseEntity<ApiResponse<ApplymentRejectResponse>> rejectApplyment(
		@PathVariable Long memberId,
		@RequestBody ApplymentRejectRequest request
	) {
		ApplymentRejectResponse response = adminMentorApplymentService.rejectApplyment(
			memberId, request
		);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(
				true,
				HttpStatus.OK,
				"멘토 지원 거절",
				response
			));
	}
}
