package com.dementor.domain.job.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "직무 생성 Request")
public record JobCreaeteRequest(
        @Schema(description = "직무 이름", example = "C++ 개발자")
        String jobName
) {
}
