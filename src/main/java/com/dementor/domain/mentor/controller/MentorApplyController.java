package com.dementor.domain.mentor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dementor.domain.mentor.dto.response.MentorApplyResponse;
import com.dementor.domain.mentor.service.MentorApplyService;
import com.dementor.global.ApiResponse;
import com.dementor.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "멘토 신청 관리", description = "멘토링 신청 조회 및 승인/거절 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentor/apply")
public class MentorApplyController {

	private final MentorApplyService mentorApplyService;

	// 멘토 신청 목록 조회
	@Operation(summary = "신청된 목록 조회", description = "신청된 목록을 조회합니다")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('MENTOR')")
	public ApiResponse<MentorApplyResponse.GetApplyMenteePageList> getApplyByMentor(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		MentorApplyResponse.GetApplyMenteePageList response = mentorApplyService.getApplyByMentor(userDetails.getId(), page-1, size);

		return ApiResponse.of(true, HttpStatus.OK, "신청된 목록을 조회했습니다", response);
	}
}
