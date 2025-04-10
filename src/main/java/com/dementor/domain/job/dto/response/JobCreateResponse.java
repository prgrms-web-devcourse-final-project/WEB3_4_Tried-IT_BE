package com.dementor.domain.job.dto.response;

public record JobCreateResponse(
    Long jobId,
    String name
) {
    public static JobCreateResponse of(Long jobId, String name) {
        return new JobCreateResponse(jobId, name);
    }
}
