package com.dementor.domain.job.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "직무명 수정")
public record JobUpdateResponse(
        @Schema(description = "직무명", example = "게임 개발자")
        String jobName
) {
}
