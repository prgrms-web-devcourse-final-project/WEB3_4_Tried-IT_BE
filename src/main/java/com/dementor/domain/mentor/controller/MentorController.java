package com.dementor.domain.mentor.controller;

import com.dementor.domain.apply.entity.ApplyStatus;
import com.dementor.domain.mentor.dto.request.MentorApplyProposalRequest;
import com.dementor.domain.mentor.dto.request.MentorApplyStatusRequest;
import com.dementor.domain.mentor.dto.request.MentorChangeRequest;
import com.dementor.domain.mentor.dto.response.*;
import com.dementor.domain.mentor.exception.MentorException;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentor.service.MentorService;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentResponse;
import com.dementor.domain.mentorapplyproposal.repository.MentorApplyProposalRepository;
import com.dementor.domain.mentoreditproposal.dto.MentorEditProposalRequest;
import com.dementor.domain.mentoreditproposal.dto.MentorEditUpdateRenewalResponse;
import com.dementor.domain.mentoringclass.service.MentoringClassService;
import com.dementor.domain.postattachment.exception.PostAttachmentException;
import com.dementor.domain.postattachment.service.PostAttachmentService;
import com.dementor.global.ApiResponse;
import com.dementor.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
@Tag(name = "멘토 API", description = "멘토 지원, 정보 수정, 조회 API")
public class MentorController {
	private final MentorService mentorService;
	private final MentorRepository mentorRepository;
	private final MentoringClassService mentoringClassService;
	private final PostAttachmentService postAttachmentService;
	private final MentorApplyProposalRepository mentorApplyProposalRepository;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('MENTEE') and #requestDto.memberId() == authentication.principal.id")
	@Operation(summary = "멘토 지원", description = "새로운 멘토 지원 API")
	public ResponseEntity<ApiResponse<?>> applyMentor(
		@RequestPart(value = "mentorApplyData") @Valid MentorApplyProposalRequest.MentorApplyProposalRequestDto requestDto,
		@RequestPart(value = "files", required = false) List<MultipartFile> files,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		try {
			// 권한 체크: 멘토 지원 시 자신의 ID로만 지원 가능
			if (!requestDto.memberId().equals(userDetails.getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(ApiResponse.of(false, HttpStatus.FORBIDDEN, "멘토 지원은 본인만 가능합니다."));
			}

			// 파일이나 마크다운 중 하나는 필수
			if ((files == null || files.isEmpty()) && (requestDto.introduction() == null || requestDto.introduction()
				.isEmpty())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.of(false, HttpStatus.BAD_REQUEST, "파일 또는 마크다운 텍스트 중 하나는 필수입니다."));
			}

			ApplymentResponse response = mentorService.applyMentor(requestDto, files);

			return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.of(true, HttpStatus.CREATED, "멘토 지원에 성공했습니다.", response));
		} catch (MentorException e) {
			return ResponseEntity.status(e.getErrorCode().getStatus())
				.body(ApiResponse.of(false, e.getErrorCode().getStatus(), e.getMessage()));
		} catch (PostAttachmentException e) {
			return ResponseEntity.status(e.getErrorCode().getStatus())
				.body(ApiResponse.of(false, e.getErrorCode().getStatus(), e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.of(false, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."));
		}
	}

	@PutMapping(value = "/{memberId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('MENTOR') and #memberId == authentication.principal.id")
	@Operation(summary = "멘토 정보 수정", description = "멘토 정보 수정 API - 로그인한 멘토 본인만 가능")
	public ResponseEntity<ApiResponse<?>> updateMentor(
		@PathVariable Long memberId,
		@RequestPart(value = "mentorUpdateData") @Valid MentorEditProposalRequest requestDto,
		@RequestPart(value = "files", required = false) List<MultipartFile> files,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		try {
			boolean exists = mentorRepository.existsById(memberId);
			if (!exists) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.of(false, HttpStatus.NOT_FOUND, "해당 멘토를 찾을 수 없습니다: " + memberId));
			}

			// 권한 체크: 로그인한 사용자와 요청된 멘토 ID가 일치하는지
			if (!memberId.equals(userDetails.getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(ApiResponse.of(false, HttpStatus.FORBIDDEN, "해당 멘토 정보를 수정할 권한이 없습니다."));
			}

			// 파일이나 마크다운 중 하나는 필수
			if ((files == null || files.isEmpty()) && (requestDto.getIntroduction() == null
				|| requestDto.getIntroduction().isEmpty())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.of(false, HttpStatus.BAD_REQUEST, "파일 또는 마크다운 텍스트 중 하나는 필수입니다."));
			}

			MentorEditUpdateRenewalResponse response = mentorService.updateMentor(memberId, requestDto, files);

			return ResponseEntity.ok()
				.body(ApiResponse.of(true, HttpStatus.OK, "멘토 정보 수정 요청에 성공했습니다.", response));
		} catch (MentorException e) {
			return ResponseEntity.status(e.getErrorCode().getStatus())
				.body(ApiResponse.of(false, e.getErrorCode().getStatus(), e.getMessage()));
		} catch (PostAttachmentException e) {
			return ResponseEntity.status(e.getErrorCode().getStatus())
				.body(ApiResponse.of(false, e.getErrorCode().getStatus(), e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.of(false, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."));
		}
	}

	@GetMapping("/{memberId}/info")
	@PreAuthorize("hasRole('MENTOR') and #memberId == authentication.principal.id")
	@Operation(summary = "멘토 정보 조회", description = "특정 멘토의 상세 정보 조회 API - 로그인한 멘토 본인만 가능")
	public ResponseEntity<ApiResponse<?>> getMentorInfo(
		@PathVariable Long memberId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		try {
			boolean exists = mentorRepository.existsById(memberId);
			if (!exists) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.of(false, HttpStatus.NOT_FOUND, "해당 멘토를 찾을 수 없습니다: " + memberId));
			}

			// 권한 체크: 로그인한 사용자와 요청된 멘토 ID가 일치하는지
			if (!memberId.equals(userDetails.getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(ApiResponse.of(false, HttpStatus.FORBIDDEN, "해당 멘토 정보를 조회할 권한이 없습니다."));
			}

			MentorInfoResponse mentorInfo = mentorService.getMentorInfo(memberId);

			return ResponseEntity.ok()
				.body(ApiResponse.of(true, HttpStatus.OK, "멘토 정보 조회에 성공했습니다.", mentorInfo));
		} catch (MentorException e) {
			return ResponseEntity.status(e.getErrorCode().getStatus())
				.body(ApiResponse.of(false, e.getErrorCode().getStatus(), e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.of(false, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."));
		}
	}

	@GetMapping("/{memberId}/modification-requests")
	@PreAuthorize("(hasRole('MENTOR') and #memberId == authentication.principal.id) or hasRole('ADMIN')")
	// 본인 멘토 또는 관리자만 조회 가능
	@Operation(summary = "멘토 정보 수정 요청 조회", description = "특정 멘토의 정보 수정 요청 이력과 상태를 조회합니다. - 로그인한 멘토만 가능")
	public ResponseEntity<ApiResponse<?>> getModificationRequests(
		@PathVariable Long memberId,
		@RequestParam(required = false) String status,
		@RequestParam(required = false, defaultValue = "1") Integer page,
		@RequestParam(required = false, defaultValue = "10") Integer size,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		try {
			boolean exists = mentorRepository.existsById(memberId);
			if (!exists) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.of(false, HttpStatus.NOT_FOUND, "해당 멘토를 찾을 수 없습니다: " + memberId));
			}

			// 페이지 번호와 크기 유효성 검사
			if (page < 1 || size < 1) {
				Map<String, String> errors = new HashMap<>();
				if (page < 1)
					errors.put("page", "페이지 번호는 1 이상이어야 합니다.");
				if (size < 1)
					errors.put("size", "페이지 크기는 1 이상이어야 합니다.");
				return ResponseEntity.badRequest()
					.body(ApiResponse.of(false, HttpStatus.BAD_REQUEST, "멘토 정보 수정 요청 목록 조회에 실패했습니다.", errors));
			}

			// 상태 유효성 검사
			if (status != null && !List.of("PENDING", "APPROVED", "REJECTED").contains(status)) {
				Map<String, String> errors = new HashMap<>();
				errors.put("status", "유효하지 않은 상태값입니다.");
				return ResponseEntity.badRequest()
					.body(ApiResponse.of(false, HttpStatus.BAD_REQUEST, "멘토 정보 수정 요청 목록 조회에 실패했습니다.", errors));
			}

			MentorChangeRequest.ModificationRequestParams params =
				new MentorChangeRequest.ModificationRequestParams(status, page, size);
			MentorChangeResponse.ChangeListResponse response =
				mentorService.getModificationRequests(memberId, params);

			return ResponseEntity.ok()
				.body(ApiResponse.of(true, HttpStatus.OK, "멘토 정보 수정 요청 목록 조회에 성공했습니다.", response));
		} catch (MentorException e) {
			return ResponseEntity.status(e.getErrorCode().getStatus())
				.body(ApiResponse.of(false, e.getErrorCode().getStatus(), e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.of(false, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."));
		}
	}

	// MentorApplyController에서 가져온 메소드
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

	// MentorControllerFromHo에서 가져온 메소드
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
