package com.dementor.domain.mentoringclass.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.dementor.domain.mentoringclass.dto.request.MentoringClassCreateRequest;
import com.dementor.domain.mentoringclass.dto.request.MentoringClassUpdateRequest;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassDetailResponse;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassFindResponse;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassUpdateResponse;
import com.dementor.domain.mentoringclass.service.MentoringClassService;
import com.dementor.global.ApiResponse;
import com.dementor.global.common.pagination.PaginationUtil;
import com.dementor.global.common.swaggerDocs.MentoringClassSwagger;
import com.dementor.global.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
@Slf4j
public class MentoringClassController implements MentoringClassSwagger {
    private final MentoringClassService mentoringClassService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MentoringClassFindResponse>>> getClass(
            @RequestParam(required = false) List<String> jobId,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Pageable domainPageable = PaginationUtil.getMentoringClassPageable(pageable);

        // String List를 Long List로 변환
        List<Long> jobIds = jobId != null ?
                jobId.stream()
                        .map(Long::parseLong)
                        .toList() :
                null;

        Page<MentoringClassFindResponse> result = mentoringClassService.findAllClass(jobIds, domainPageable);

        if (result.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.of(
                            true,
                            HttpStatus.OK,
                            "조회된 멘토링 수업이 없습니다.",
                            result
                    ));
        } else {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.of(
                            true,
                            HttpStatus.OK,
                            "멘토링 수업 조회 성공",
                            result
                    ));
        }

    }

    @Override
    @GetMapping("/{classId}")
    public ResponseEntity<ApiResponse<MentoringClassDetailResponse>> getClassById(
            @PathVariable Long classId
    ) {
        MentoringClassDetailResponse response = mentoringClassService.findOneClass(classId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.of(
                        true,
                        HttpStatus.OK,
                        "멘토링 수업 상세 조회 성공",
                        response
                ));
    }

    @Override
    @PreAuthorize("hasRole('MENTOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<MentoringClassDetailResponse>> createClass(
            @RequestBody MentoringClassCreateRequest request,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long memberId = userDetails.getId();

        MentoringClassDetailResponse response = mentoringClassService.createClass(memberId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        true,
                        HttpStatus.CREATED,
                        "멘토링 클래스 생성 성공",
                        response
                ));
    }

    @Override
    @PreAuthorize("hasRole('MENTOR')")
    @PutMapping("/{classId}")
    public ResponseEntity<ApiResponse<MentoringClassUpdateResponse>> updateClass(
            @PathVariable Long classId,
            @RequestBody MentoringClassUpdateRequest request,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long memberId = userDetails.getId();

        MentoringClassUpdateResponse response = mentoringClassService.updateClass(classId, memberId, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.of(
                        true,
                        HttpStatus.OK,
                        "멘토링 클래스 수정 성공",
                        response
                ));
    }

    @Override
    @PreAuthorize("hasRole('MENTOR')")
    @DeleteMapping("/{classId}")
    public ResponseEntity<ApiResponse<?>> deleteClass(
            @PathVariable Long classId
    ) {
        mentoringClassService.deleteClass(classId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.of(
                        true,
                        HttpStatus.NO_CONTENT,
                        "멘토링 수업 삭제 성공",
                        null
                ));
    }


}
