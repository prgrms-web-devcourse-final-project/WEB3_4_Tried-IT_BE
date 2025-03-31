package com.dementor.domain.mentor.dto.request;

import com.dementor.domain.mentor.entity.Mentor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MentorRequestDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Create {
        private Long jobId;
        private Long userId;
        private String name;
        private String currentCompany;
        private Integer career;
        private String phone;
        private String introduction;
        private String bestFor;

        // 파일 첨부 정보
        private AttachmentInfo attachment;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class AttachmentInfo {
            private String filename;
            private String storeFilePath;
            private Long fileSize;
        }

        // MentorEntity 변환 메서드
        public Mentor toEntity() {
            return Mentor.builder()
                    .name(name)
                    .currentCompany(currentCompany)
                    .career(career)
                    .phone(phone)
                    .introduction(introduction)
                    .bestFor(bestFor)
                    .isApproved(Mentor.ApprovalStatus.N)
                    .isModified(Mentor.ApprovalStatus.N)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Update {
        private Long mentorId;
        private Long jobId;
        private String name;
        private Integer career;
        private String phone;
        private String introduction;

        // 기존 첨부파일 ID (삭제를 위한)
        private Long removeAttachmentId;

        // 새로운 첨부파일 정보
        private Create.AttachmentInfo newAttachment;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Approve {
        private Long mentorId;
        private Mentor.ApprovalStatus approve;
    }
}
