package com.dementor.domain.postattachment.controller;

import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposal;
import com.dementor.domain.mentorapplyproposal.repository.MentorApplyProposalRepository;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposal;
import com.dementor.domain.mentoreditproposal.repository.MentorEditProposalRepository;
import com.dementor.domain.postattachment.dto.response.FileResponse;
import com.dementor.domain.postattachment.dto.response.FileResponse.FileInfoDto;
import com.dementor.domain.postattachment.exception.PostAttachmentException;
import com.dementor.domain.postattachment.service.PostAttachmentService;
import com.dementor.global.ApiResponse;
import com.dementor.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "멘토 파일첨부 API", description = "멘토 지원, 정보 수정에 파일 첨부할 수 있습니다.")
public class PostAttachmentController {

    private final PostAttachmentService postAttachmentService;
    private final MentorApplyProposalRepository mentorApplyProposalRepository;
    private final MentorEditProposalRepository mentorEditProposalRepository;

    //파일 업로드 API
    @PostMapping(value = "/upload")
    @PreAuthorize("hasRole('MENTEE') or hasRole('MENTOR')")
    @Operation(summary = "마크다운 이미지 업로드", description = "멘토 지원 및 정보 수정 시 마크다운에 포함된 이미지를 업로드합니다.")
    public ResponseEntity<ApiResponse<?>> uploadMarkdownContent(
            @RequestParam(value = "introductionMarkdown", required = false) String introduction,
            @RequestParam(value = "proposalId", required = true) Long proposalId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            // 마크다운 텍스트는 필수
            if (introduction == null || introduction.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.of(false, HttpStatus.BAD_REQUEST, "마크다운 텍스트는 필수입니다."));
            }

            // 권한 체크: proposal 소유자 확인
            boolean isOwner = false;

            // 먼저 멘토 지원서 확인
            Optional<MentorApplyProposal> optionalApplyProposal = mentorApplyProposalRepository.findById(proposalId);
            if (optionalApplyProposal.isPresent()) {
                MentorApplyProposal applyProposal = optionalApplyProposal.get();
                // 본인 소유 지원서인지 확인
                if (applyProposal.getMember().getId().equals(userDetails.getId())) {
                    isOwner = true;
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.of(false, HttpStatus.FORBIDDEN, "본인의 지원서만 수정할 수 있습니다."));
                }
            } else {
                // 멘토 수정 요청 확인
                Optional<MentorEditProposal> optionalEditProposal = mentorEditProposalRepository.findById(proposalId);
                if (optionalEditProposal.isPresent()) {
                    MentorEditProposal editProposal = optionalEditProposal.get();
                    // 본인 소유 수정 요청인지 확인
                    if (editProposal.getMember().getId().equals(userDetails.getId())) {
                        isOwner = true;
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.of(false, HttpStatus.FORBIDDEN, "본인의 수정 요청만 변경할 수 있습니다."));
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.of(false, HttpStatus.NOT_FOUND, "멘토 지원서 또는 정보 수정 요청을 찾을 수 없습니다."));
                }
            }

            if (!isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.of(false, HttpStatus.FORBIDDEN, "권한이 없습니다."));
            }

            List<FileInfoDto> allUploadedFiles = postAttachmentService.uploadMarkdownContent(
                    introduction, proposalId);

            FileResponse.FileUploadResponseDto responseDto = FileResponse.FileUploadResponseDto.builder()
                    .status(HttpStatus.CREATED.value())
                    .message("업로드에 성공했습니다.")
                    .data(allUploadedFiles)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.of(true, HttpStatus.CREATED, "업로드에 성공했습니다.", responseDto));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(false, HttpStatus.BAD_REQUEST, e.getMessage()));
        } catch (PostAttachmentException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus().value())
                    .body(ApiResponse.of(false, e.getErrorCode().getStatus(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(false, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    //파일 삭제 API
    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasRole('ADMIN') or @postAttachmentService.isFileOwner(#attachmentId, authentication.principal.id)")
    // 파일 업로드한 본인과 관리자만 파일 삭제 가능
    @Operation(summary = "파일 삭제", description = "특정 파일을 삭제합니다. 파일 업로드한 본인만 삭제할 수 있습니다.")
    public ResponseEntity<ApiResponse<?>> deleteFile(
            @PathVariable("attachmentId") Long attachmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            // 파일 삭제 처리
            postAttachmentService.deleteFile(attachmentId, userDetails.getId());

            FileResponse.FileDeleteResponseDto responseDto = FileResponse.FileDeleteResponseDto.builder()
                    .status(HttpStatus.OK.value())
                    .message("파일 삭제에 성공했습니다.")
                    .build();

            // 응답 생성
            return ResponseEntity.ok()
                    .body(ApiResponse.of(true, HttpStatus.OK, "파일 삭제에 성공했습니다.", responseDto));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(false, HttpStatus.BAD_REQUEST, e.getMessage()));
        } catch (PostAttachmentException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus().value())
                    .body(ApiResponse.of(false, e.getErrorCode().getStatus(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(false, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    //일반 첨부 파일 다운로드 API
    @GetMapping("/{attachmentId}/download")
    @PreAuthorize("hasRole('ADMIN') or @postAttachmentService.isFileOwner(#attachmentId, authentication.principal.id)")
    @Operation(summary = "일반 첨부 파일 다운로드", description = "특정 일반 첨부 파일을 다운로드합니다. 멘토와 관리자만 다운로드가 가능합니다.")
    public ResponseEntity<?> downloadFile(
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            // 파일 다운로드 처리
            Map<String, Object> fileInfo = postAttachmentService.downloadFile(attachmentId, userDetails.getId());

            Resource resource = (Resource) fileInfo.get("resource");
            String contentType = (String) fileInfo.get("contentType");
            String fileName = (String) fileInfo.get("fileName");

            // 파일명 인코딩 처리 (ASCII 범위 내 문자만 포함하는 파일명)
            String encodedFileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                    .body(resource);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(false, HttpStatus.BAD_REQUEST, e.getMessage()));
        } catch (PostAttachmentException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus().value())
                    .body(ApiResponse.of(false, e.getErrorCode().getStatus(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(false, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    //마크다운 이미지 다운로드 API
    @GetMapping("/markdown-images/{uniqueIdentifier}")
    @PreAuthorize("hasRole('ADMIN') or @postAttachmentService.isMarkdownImageOwner(#uniqueIdentifier, authentication.principal.id)")
    @Operation(summary = "마크다운 이미지 다운로드", description = "마크다운 텍스트 내에서 참조하는 이미지를 제공합니다. 이 API는 이미지를 inline으로 표시합니다.")
    public ResponseEntity<?> downloadMarkdownImage(
            @PathVariable String uniqueIdentifier,
            @RequestParam(required = false) Integer width,
            @RequestParam(required = false) Integer height,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            Map<String, Object> imageInfo = postAttachmentService.downloadMarkdownImage(uniqueIdentifier, width, height);

            Resource resource = (Resource) imageInfo.get("resource");
            String contentType = (String) imageInfo.get("contentType");
            String fileName = (String) imageInfo.get("fileName");

            // 내부 및 외부 이미지를 동일하게 처리
            String encodedFileName = "";
            if (fileName != null && !fileName.isEmpty()) {
                encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                        .replaceAll("\\+", "%20");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline" +
                            (encodedFileName.isEmpty() ? "" : "; filename*=UTF-8''" + encodedFileName))
                    .body(resource);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(false, HttpStatus.BAD_REQUEST, e.getMessage()));
        } catch (PostAttachmentException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus().value())
                    .body(ApiResponse.of(false, e.getErrorCode().getStatus(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(false, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}