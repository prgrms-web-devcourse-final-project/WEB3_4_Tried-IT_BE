package com.dementor.global.common.swaggerDocs;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.dementor.domain.mentoringclass.dto.request.MentoringClassCreateRequest;
import com.dementor.domain.mentoringclass.dto.request.MentoringClassUpdateRequest;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassDetailResponse;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassFindResponse;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassUpdateResponse;
import com.dementor.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "멘토링 수업", description = "멘토링 수업 관리")
public interface MentoringClassSwagger {
	@Operation(summary = "멘토링 수업 전체 조회", description = "모든 멘토링 수업을 조회합니다.")
	ResponseEntity<ApiResponse<Page<MentoringClassFindResponse>>> getClass(
		List<String> jobId,
		@Parameter(description = "페이지 정보", example = """
			{
			  "page": 1,
			  "size": 10,
			  "sort": "id,desc"
			}
			""") Pageable pageable);

	@Operation(summary = "멘토링 수업 상세 조회", description = "특정 멘토링 수업의 상세 정보를 조회합니다.")
	ResponseEntity<ApiResponse<MentoringClassDetailResponse>> getClassById(Long classId);

	@Operation(summary = "멘토링 수업 등록", description = "멘토가 멘토링 수업을 등록합니다.")
	ResponseEntity<ApiResponse<MentoringClassDetailResponse>> createClass(
		MentoringClassCreateRequest request,
		Authentication authentication);

	@Operation(summary = "멘토링 수업 수정", description = "멘토링 수업 정보를 수정합니다.")
	ResponseEntity<ApiResponse<MentoringClassUpdateResponse>> updateClass(
		Long classId,
		MentoringClassUpdateRequest request,
		Authentication authentication);

	@Operation(summary = "멘토링 수업 삭제", description = "멘토링 수업을 삭제합니다.")
	ResponseEntity<ApiResponse<?>> deleteClass(Long classId);
}
