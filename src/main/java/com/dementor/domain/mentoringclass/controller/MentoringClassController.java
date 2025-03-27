package com.dementor.domain.mentoringclass.controller;

import com.dementor.domain.mentoringclass.dto.response.MentoringClassFindResponse;
import com.dementor.domain.mentoringclass.service.MentoringClassService;
import com.dementor.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "멘토링 수업", description = "멘토링 수업 관리")
@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class MentoringClassController {
    private final MentoringClassService mentoringClassService;

    @Operation(summary = "멘토링 수업 전체 조회", description = "모든 멘토링 수업을 조회합니다.")
    @GetMapping // 전체 조회
    public ApiResponse<?> getClass(
            @RequestParam(required = false) Long jobId
    ) {
        List<MentoringClassFindResponse> list = mentoringClassService.selectClass(jobId);
        return ApiResponse.success(
                "멘토링 수업 조회 성공",
                list
        );
    }

    // TODO : /api/mentor/class/{mentor_id} 로 바꿀 예정
    @GetMapping("/{mentor_id}")
    public ApiResponse<?> getClassByMentorId(
        @PathVariable(required = false) Long mentorId
    ) {
        return null;
    }

    @GetMapping("/{class_id}")
    public ApiResponse<?> getClassById(
            @PathVariable Long classId
    ) {
        return null;
    }

    @Operation(summary = "멘토링 수업 등록", description = "멘토가 멘토링 수업을 등록합니다.")
    @PostMapping
    public ApiResponse<?> createClass(
//            @RequestBody MentoringClassCreateRequest request
            ) {

        return ApiResponse.success("멘토링 클래스 생성 성공", "생성된 클래스 ID");
    }

    @PutMapping("/{class_id}")
    public ApiResponse<?> updateClass(
            @PathVariable Long classId
    ) {
        // TODO: 실제 생성 로직 구현
        return ApiResponse.success("멘토링 클래스 수정 성공", "생성된 클래스 ID");
    }

    @DeleteMapping("/{class_id}")
    public ApiResponse<?> deleteClass(
            @PathVariable Long classId
    ) {
        // TODO: 실제 생성 로직 구현
        return ApiResponse.success("멘토링 클래스 삭제 성공", "생성된 클래스 ID");
    }
}
