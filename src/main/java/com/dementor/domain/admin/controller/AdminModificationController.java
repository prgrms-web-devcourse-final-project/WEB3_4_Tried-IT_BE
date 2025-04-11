package com.dementor.domain.admin.controller;

import com.dementor.domain.mentoreditproposal.dto.MentorEditFindAllRenewalResponse;
import com.dementor.domain.mentoreditproposal.dto.MentorEditUpdateRenewalResponse;
import com.dementor.domain.mentoreditproposal.service.AdminModificationService;
import com.dementor.global.ApiResponse;
import com.dementor.global.common.pagination.PaginationUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admain/mentor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminModificationController {

	private final AdminModificationService adminModificationService;

	@GetMapping("/modifyList")
	//@PreAuthorize("hasRole('ADMIN')") / 수정 요청 목록 전체조회
	public ResponseEntity<ApiResponse<Page<MentorEditFindAllRenewalResponse>>> findAllModificationRequest(
		Pageable pageable
	) {
		Pageable domainPageable = PaginationUtil.getModificationPageable(pageable);

		Page<MentorEditFindAllRenewalResponse> result = adminModificationService.findAllModificationRequest(
			domainPageable);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(
				true,
				HttpStatus.OK,
				"멘토 정보 수정 요청 전체 조회",
				result
			));
	}

	@PutMapping("/modify/{memberId}/approve")
	public ResponseEntity<ApiResponse<MentorEditUpdateRenewalResponse>> approveMentorUpdate(
		@PathVariable Long memberId
	) {
		MentorEditUpdateRenewalResponse response = adminModificationService.approveMentorUpdate(memberId);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(
				true,
				HttpStatus.OK,
				"수정 성공",
				response
			));
	}

	@PutMapping("/modify/{memberId}/reject")
	public ResponseEntity<ApiResponse<MentorEditUpdateRenewalResponse>> rejectMentorUpdate(
		@PathVariable Long memberId
	) {
		MentorEditUpdateRenewalResponse response = adminModificationService.rejectMentorUpdate(memberId);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(
				true,
				HttpStatus.OK,
				"수정 성공",
				response
			));
	}

}
