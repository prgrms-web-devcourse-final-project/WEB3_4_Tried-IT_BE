package com.dementor.domain.postattachment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class FileResponse {
    // 파일 업로드 응답 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FileUploadResponseDto {
        private int status;
        private String message;
        private List<FileInfoDto> data;
    }

    // 파일 정보 DTO (업로드 응답에 포함되는 개별 파일 정보)
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FileInfoDto {
        private Long attachmentId;
        private String originalFilename;
        private Long fileSize;
        private String fileUrl;
        private String uniqueIdentifier;
    }

    // 파일 삭제 응답 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FileDeleteResponseDto {
        private int status;
        private String message;
    }
}
