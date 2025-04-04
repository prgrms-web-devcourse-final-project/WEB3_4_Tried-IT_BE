package com.dementor.domain.mentoringclass.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dementor.domain.mentoringclass.dto.SortDirection;
import com.dementor.domain.mentoringclass.dto.request.MentoringClassCreateRequest;
import com.dementor.domain.mentoringclass.dto.request.MentoringClassUpdateRequest;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassDetailResponse;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassFindResponse;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassUpdateResponse;
import com.dementor.domain.mentoringclass.service.MentoringClassService;
import com.dementor.global.ApiResponse;
import com.dementor.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "멘토링 수업", description = "멘토링 수업 관리")
@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
@Slf4j
public class MentoringClassController {
    private final MentoringClassService mentoringClassService;
    private final PagedResourcesAssembler<MentoringClassFindResponse> pagedResourcesAssembler;

    @Operation(summary = "멘토링 수업 전체 조회", description = "모든 멘토링 수업을 조회합니다.")
    @GetMapping
    public ApiResponse<?> getClass(
            @RequestParam(required = false) Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") SortDirection order
    ) {
        Page<MentoringClassFindResponse> result = mentoringClassService.findAllClass(jobId, page, size, sortBy, order);
        PagedModel<EntityModel<MentoringClassFindResponse>> pagedModel = pagedResourcesAssembler.toModel(result);

        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "멘토링 수업 조회 성공",
                pagedModel
        );
    }

    // TODO : /api/mentor/class/{mentor_id} 멘토 도메인으로 옮겨야 함.
//    @Operation(summary = "멘토가 등록한 수업 조회", description = "멘토가 자신의 수업을 조회합니다.")
//    @GetMapping("/{mentor_id}")
//    public ApiResponse<?> getClassByMentorId(
//        @PathVariable(required = false) Long mentorId
//    ) {
//        return null;
//    }

    @Operation(summary = "멘토링 수업 상세 조회", description = "특정 멘토링 수업의 상세 정보를 조회합니다.")
    @GetMapping("/{classId}")
    public ApiResponse<?> getClassById(
            @PathVariable Long classId
    ) {
        MentoringClassDetailResponse mentoringClass = mentoringClassService.findOneClass(classId);
        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "멘토링 수업 상세 조회 성공",
                mentoringClass
        );
    }

    @Operation(summary = "멘토링 수업 등록", description = "멘토가 멘토링 수업을 등록합니다.")
    @PreAuthorize("hasRole('MENTOR')")
    @PostMapping
    public ApiResponse<?> createClass(
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
    public ApiResponse<?> updateClass(
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
