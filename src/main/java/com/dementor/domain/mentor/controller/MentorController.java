package com.dementor.domain.mentor.controller;

import com.dementor.domain.mentor.dto.request.MentorApplicationRequest;
import com.dementor.domain.mentor.dto.request.MentorChangeRequest;
import com.dementor.domain.mentor.dto.request.MentorUpdateRequest;
import com.dementor.domain.mentor.dto.response.MentorChangeResponse;
import com.dementor.domain.mentor.dto.response.MentorInfoResponse;
import com.dementor.domain.mentor.dto.response.MentorUpdateResponse;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.exception.MentorException;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentor.service.MentorService;
import com.dementor.domain.postattachment.exception.PostAttachmentException;
import com.dementor.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MENTEE') and #requestDto.memberId() == authentication.principal.id")
    @Operation(summary = "멘토 지원", description = "새로운 멘토 지원 API")
    public ResponseEntity<ApiResponse<?>> applyMentor(
            @RequestPart("mentorInfo") @Valid MentorApplicationRequest.MentorApplicationRequestDto requestDto,
            @RequestPart(value = "introductionImages", required = false) List<MultipartFile> introductionImages,
            @RequestPart(value = "bestForImages", required = false) List<MultipartFile> bestForImages,
            @RequestPart(value = "attachmentFiles", required = false) List<MultipartFile> attachmentFiles) {

        try {
            mentorService.applyMentor(requestDto, introductionImages, bestForImages, attachmentFiles);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.of(true, HttpStatus.CREATED, "멘토 지원에 성공했습니다."));
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

    @PutMapping(value = "/{memberId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MENTOR') and #memberId == authentication.principal.id")
    @Operation(summary = "멘토 정보 수정", description = "멘토 정보 수정 API - 로그인한 멘토 본인만 가능")
    public ResponseEntity<ApiResponse<?>> updateMentor(
            @PathVariable Long memberId,
            @RequestPart("mentorInfo") @Valid MentorUpdateRequest.MentorUpdateRequestDto requestDto,
            @RequestPart(value = "introductionImages", required = false) List<MultipartFile> introductionImages,
            @RequestPart(value = "bestForImages", required = false) List<MultipartFile> bestForImages,
            @RequestPart(value = "attachmentFiles", required = false) List<MultipartFile> attachmentFiles) {

        try {
            boolean exists = mentorRepository.existsById(memberId);
            if (!exists) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.of(false, HttpStatus.NOT_FOUND, "해당 멘토를 찾을 수 없습니다: " + memberId));
            }
            mentorService.updateMentor(memberId, requestDto, introductionImages, bestForImages, attachmentFiles);

            MentorUpdateResponse response = MentorUpdateResponse.of(memberId, Mentor.ModificationStatus.PENDING);

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
            @PathVariable Long memberId) {

        try {
            boolean exists = mentorRepository.existsById(memberId);
            if (!exists) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.of(false, HttpStatus.NOT_FOUND, "해당 멘토를 찾을 수 없습니다: " + memberId));
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
    @PreAuthorize("hasRole('MENTOR') and #memberId == authentication.principal.id or hasRole('ADMIN')")  // 본인 멘토 또는 관리자만 조회 가능
    @Operation(summary = "멘토 정보 수정 요청 조회", description = "특정 멘토의 정보 수정 요청 이력과 상태를 조회합니다. - 로그인한 멘토만 가능")
    public ResponseEntity<ApiResponse<?>> getModificationRequests(
            @PathVariable Long memberId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        try {
            boolean exists = mentorRepository.existsById(memberId);
            if (!exists) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.of(false, HttpStatus.NOT_FOUND, "해당 멘토를 찾을 수 없습니다: " + memberId));
            }
            // 페이지 번호와 크기 유효성 검사
            if (page < 1 || size < 1) {
                Map<String, String> errors = new HashMap<>();
                if (page < 1) errors.put("page", "페이지 번호는 1 이상이어야 합니다.");
                if (size < 1) errors.put("size", "페이지 크기는 1 이상이어야 합니다.");
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
}
