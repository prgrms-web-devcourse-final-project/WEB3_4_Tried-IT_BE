package com.dementor.global.common.swaggerDocs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.dementor.domain.mentorapplyproposal.dto.request.ApplymentRejectRequest;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentApprovalResponse;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentDetailResponse;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentRejectResponse;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentResponse;
import com.dementor.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "멘토 지원 ", description = "멘토 지원 데이터 CRUD")
public interface AdminMentorApplymentSwagger {
	@Operation(summary = "멘토 지원 목록 전체조회", description = "멘토 지원자 정보를 조회합니다")
	ResponseEntity<ApiResponse<Page<ApplymentResponse>>> findAllAdminMentorApplyment(
		@Parameter(description = "페이지 정보", example = """
			{
			  "page": 1,
			  "size": 10,
			  "sort": "id,desc"
			}
			""") Pageable pageable);

	@Operation(summary = "멘토 지원 데이터 상세 조회", description = "지원 내용을 상세 조회합니다")
	ResponseEntity<ApiResponse<ApplymentDetailResponse>> findOneApplyment(Long applymentId);

	@Operation(summary = "멘토 지원 승인", description = "지원자를 멘토로 승인합니다")
	ResponseEntity<ApiResponse<ApplymentApprovalResponse>> approveApplyment(Long applymentId);

	@Operation(summary = "멘토 지원 거절", description = "멘토로 승인하지 않습니다.")
	ResponseEntity<ApiResponse<ApplymentRejectResponse>> rejectApplyment(Long memberId, ApplymentRejectRequest request);

}
