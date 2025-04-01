package com.dementor.domain.apply.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dementor.domain.apply.dto.request.ApplyRequest;
import com.dementor.domain.apply.dto.response.ApplyResponse;
import com.dementor.domain.apply.service.ApplyService;
import com.dementor.global.ApiResponse;
import com.dementor.global.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/apply")
public class ApplyController {

	private final ApplyService applyService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<?> createApply(
		@RequestBody ApplyRequest.ApplyCreateRequest req,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		ApplyResponse.GetApplyId response = applyService.createApply(req, userDetails.getId());

		return ApiResponse.of(true, HttpStatus.CREATED, "멘토링 신청이 완료되었습니다", response);
	}
}
