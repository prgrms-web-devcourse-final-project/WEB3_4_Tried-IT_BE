package com.dementor.domain.mentoringclass.dto.response;

public record PageInfoResponse(
    int totalDataCnt,
    int totalPages,
    boolean isLastPage,
    boolean isFirstPage,
    int requestPage,
    int requestSize
) {
} 