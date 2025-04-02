package com.dementor.domain.mentoringclass.controller;

import com.dementor.domain.mentoringclass.dto.request.MentoringClassCreateRequest;
import com.dementor.domain.mentoringclass.dto.request.MentoringClassUpdateRequest;
import com.dementor.domain.mentoringclass.dto.request.ScheduleUpdateRequest;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassDetailResponse;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassFindResponse;
import com.dementor.domain.mentoringclass.service.MentoringClassService;
import com.dementor.global.ApiResponse;
import com.dementor.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "멘토링 수업", description = "멘토링 수업 관리")
@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
@Slf4j
public class MentoringClassController {
    private final MentoringClassService mentoringClassService;

    @Operation(summary = "멘토링 수업 전체 조회", description = "모든 멘토링 수업을 조회합니다.")
    @GetMapping
    public ApiResponse<?> getClass(
            @RequestParam(required = false) Long jobId
    ) {
        List<MentoringClassFindResponse> list = mentoringClassService.findClass(jobId);
        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "멘토링 수업 조회 성공",
                list
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
        log.info("memberId : {}", memberId);

        Long classId = mentoringClassService.createClass(memberId, request);
        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "멘토링 클래스 생성 성공",
                classId
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
        
        mentoringClassService.updateClass(classId, memberId, request);
        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "멘토링 클래스 수정 성공",
                classId
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

    @Operation(summary = "멘토링 수업 스케줄 수정", description = "멘토링 수업의 스케줄을 수정합니다.")
    @PreAuthorize("hasRole('MENTOR')")
    @PutMapping("/{classId}/schedule")
    public ApiResponse<?> updateSchedule(
            @PathVariable Long classId,
            @RequestBody ScheduleUpdateRequest request,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long memberId = userDetails.getId();
        
        mentoringClassService.updateSchedule(classId, memberId, request);
        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "멘토링 클래스 스케줄 수정 성공",
                classId
        );
    }
}
