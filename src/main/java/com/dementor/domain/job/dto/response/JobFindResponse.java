package com.dementor.domain.job.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "직무 전체조회 응답")
public record JobFindResponse(
        @Schema(description = "직무 ID", example = "1")
        Long jobId,
        @Schema(description = "직무 이름", example = "백엔드 개발자")
        String name
) {
}
