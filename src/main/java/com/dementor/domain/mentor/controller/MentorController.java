package com.dementor.domain.mentor.controller;

import com.dementor.domain.apply.entity.ApplyStatus;
import com.dementor.domain.mentor.dto.request.MentorApplyProposalRequest;
import com.dementor.domain.mentor.dto.request.MentorApplyStatusRequest;
import com.dementor.domain.mentor.dto.request.MentorChangeRequest;
import com.dementor.domain.mentor.dto.response.*;
import com.dementor.domain.mentor.service.MentorService;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentResponse;
import com.dementor.domain.mentoreditproposal.dto.MentorEditProposalRequest;
import com.dementor.domain.mentoreditproposal.dto.MentorEditUpdateRenewalResponse;
import com.dementor.domain.mentoringclass.service.MentoringClassService;
import com.dementor.global.ApiResponse;
import com.dementor.global.common.pagination.PaginationUtil;
import com.dementor.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
@Tag(name = "멘토 API", description = "멘토 지원, 정보 수정, 조회 API")
public class MentorController {
	private final MentorService mentorService;
	private final MentoringClassService mentoringClassService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('MENTEE') and #requestDto.memberId() == authentication.principal.id")
	@Operation(summary = "멘토 지원", description = "새로운 멘토 지원 API")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ApplymentResponse> applyMentor(
		@RequestPart(value = "mentorApplyData") @Valid MentorApplyProposalRequest.MentorApplyProposalRequestDto requestDto,
		@RequestPart(value = "files", required = false) List<MultipartFile> files) {

		ApplymentResponse response = mentorService.applyMentor(requestDto, files);
		return ApiResponse.of(true, HttpStatus.CREATED, "멘토 지원에 성공했습니다.", response);
	}

	@PutMapping(value = "/{memberId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('MENTOR') and #memberId == authentication.principal.id")
	@Operation(summary = "멘토 정보 수정", description = "멘토 정보 수정 API - 로그인한 멘토 본인만 가능")
	public ApiResponse<MentorEditUpdateRenewalResponse> updateMentor(
		@PathVariable Long memberId,
		@RequestPart(value = "mentorUpdateData") @Valid MentorEditProposalRequest requestDto,
		@RequestPart(value = "files", required = false) List<MultipartFile> files) {

		MentorEditUpdateRenewalResponse response = mentorService.updateMentor(memberId, requestDto, files);
		return ApiResponse.of(true, HttpStatus.OK, "멘토 정보 수정 요청에 성공했습니다.", response);
	}

	@GetMapping("/{memberId}/info")
	@PreAuthorize("hasRole('MENTOR') and #memberId == authentication.principal.id")
	@Operation(summary = "멘토 정보 조회", description = "특정 멘토의 상세 정보 조회 API - 로그인한 멘토 본인만 가능")
	public ApiResponse<MentorInfoResponse> getMentorInfo(@PathVariable Long memberId) {
		MentorInfoResponse mentorInfo = mentorService.getMentorInfo(memberId);
		return ApiResponse.of(true, HttpStatus.OK, "멘토 정보 조회에 성공했습니다.", mentorInfo);
	}

	@GetMapping("/{memberId}/modification-requests")
	@PreAuthorize("hasRole('MENTOR') and #memberId == authentication.principal.id or hasRole('ADMIN')")
	@Operation(summary = "멘토 정보 수정 요청 조회", description = "특정 멘토의 정보 수정 요청 이력과 상태를 조회합니다. - 로그인한 멘토와 관리자만 가능")
	public ApiResponse<MentorChangeResponse.ChangeListResponse> getModificationRequests(
		@PathVariable Long memberId,
		@RequestParam(required = false) String status,
		@PageableDefault Pageable pageable) {

		Pageable domainPageable = PaginationUtil.getModificationPageable(pageable);
		MentorChangeRequest.ModificationRequestParams params =
				new MentorChangeRequest.ModificationRequestParams(
						status,
						domainPageable.getPageNumber() + 1,
						domainPageable.getPageSize()
				);

		MentorChangeResponse.ChangeListResponse response = mentorService.getModificationRequests(memberId, params);
		return ApiResponse.of(true, HttpStatus.OK, "멘토 정보 수정 요청 목록 조회에 성공했습니다.", response);
	}

	@GetMapping("/apply")
	@Operation(summary = "신청된 목록 조회", description = "신청된 목록을 조회합니다")
	@PreAuthorize("hasRole('MENTOR')")
	public ApiResponse<MentorApplyResponse.GetApplyMenteePageList> getApplyByMentor(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		MentorApplyResponse.GetApplyMenteePageList response = mentorService.getApplyByMentor(userDetails.getId(),
			page - 1, size);

		return ApiResponse.of(true, HttpStatus.OK, "신청된 목록을 조회했습니다", response);
	}

	// 자신의 멘토링 신청 승인/거절
	@Operation(summary = "신청 상태 변경", description = "멘토링 신청 상태를 변경합니다 (승인/거절)")
	@PutMapping("/apply/{applyId}/status")
	@PreAuthorize("hasRole('MENTOR')")
	public ApiResponse<MentorApplyStatusResponse> updateApplyStatus(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable(name = "applyId") Long applyId,
		@RequestBody MentorApplyStatusRequest request
	) {
		MentorApplyStatusResponse response = mentorService.updateApplyStatus(userDetails.getId(), applyId, request);

		String message;
		if (response.getStatus() == ApplyStatus.APPROVED) {
			message = "멘토링 신청이 승인되었습니다.";
		} else {
			message = "멘토링 신청이 거절되었습니다.";
		}

		return ApiResponse.of(true, HttpStatus.OK, message, response);
	}

	@GetMapping("/class/{memberId}")
	@Operation(summary = "멘토링 수업 조회", description = "멘토의 멘토링 수업 목록을 조회합니다")
	public ApiResponse<List<MyMentoringResponse>> getMentorClassFromMentor(
		@PathVariable Long memberId
	) {
		List<MyMentoringResponse> response = mentoringClassService.getMentorClassFromMentor(memberId);

		return ApiResponse.of(
			true,
			HttpStatus.OK,
			"My 멘토링 수업 조회 성공",
			response
		);
	}
}
