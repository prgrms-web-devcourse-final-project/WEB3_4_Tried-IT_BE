package com.dementor.domain.mentoringclass.controller;

import com.dementor.domain.mentoringclass.dto.request.MentoringClassCreateRequest;
import com.dementor.domain.mentoringclass.dto.request.MentoringClassUpdateRequest;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassDetailResponse;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassFindResponse;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassUpdateResponse;
import com.dementor.domain.mentoringclass.service.MentoringClassService;
import com.dementor.global.ApiResponse;
import com.dementor.global.common.pagination.PaginationUtil;
import com.dementor.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "멘토링 수업", description = "멘토링 수업 관리")
@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
@Slf4j
public class MentoringClassController {
    /*
     * TODO : 예외처리 계층화, 공통 로직 분리(페이징 끝), 엔티티 수정(setter 삭제), onetomany 삭제 (멘토링 - 스케줄)
     *
     * */

    private final MentoringClassService mentoringClassService;

    @Operation(summary = "멘토링 수업 전체 조회", description = "모든 멘토링 수업을 조회합니다.")
    @GetMapping
    public ApiResponse<Page<MentoringClassFindResponse>> getClass(
            @RequestParam(required = false) Long jobId,
            Pageable pageable
    ) {
        Pageable domainPageable = PaginationUtil.getMentoringClassPageable(pageable);

        Page<MentoringClassFindResponse> result = mentoringClassService.findAllClass(jobId, domainPageable);

        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "멘토링 수업 조회 성공",
                result
        );
    }

    @Operation(summary = "멘토링 수업 상세 조회", description = "특정 멘토링 수업의 상세 정보를 조회합니다.")
    @GetMapping("/{classId}")
    public ApiResponse<MentoringClassDetailResponse> getClassById(
            @PathVariable Long classId
    ) {
        MentoringClassDetailResponse response = mentoringClassService.findOneClass(classId);
        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "멘토링 수업 상세 조회 성공",
                response
        );
    }

    @Operation(summary = "멘토링 수업 등록", description = "멘토가 멘토링 수업을 등록합니다.")
    @PreAuthorize("hasRole('MENTOR')")
    @PostMapping
    public ApiResponse<MentoringClassDetailResponse> createClass(
            @RequestBody MentoringClassCreateRequest request,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long memberId = userDetails.getId();

        MentoringClassDetailResponse response = mentoringClassService.createClass(memberId, request);
        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "멘토링 클래스 생성 성공",
                response
        );
    }

    @Operation(summary = "멘토링 수업 수정", description = "멘토링 수업 정보를 수정합니다.")
    @PreAuthorize("hasRole('MENTOR')")
    @PutMapping("/{classId}")
    public ApiResponse<MentoringClassUpdateResponse> updateClass(
            @PathVariable Long classId,
            @RequestBody MentoringClassUpdateRequest request,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long memberId = userDetails.getId();
        
        MentoringClassUpdateResponse response = mentoringClassService.updateClass(classId, memberId, request);
        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "멘토링 클래스 수정 성공",
                response
        );
    }

    @Operation(summary = "멘토링 수업 삭제", description = "멘토링 수업을 삭제합니다.")
    @PreAuthorize("hasRole('MENTOR')")
    @DeleteMapping("/{classId}")
    public ApiResponse<?> deleteClass(
            @PathVariable Long classId
    ) {
        mentoringClassService.deleteClass(classId);
        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "멘토링 수업 삭제 성공",
                null
        );
    }
}
