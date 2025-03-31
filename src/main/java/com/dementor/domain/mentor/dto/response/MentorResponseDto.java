package com.dementor.domain.mentor.dto.response;

import com.dementor.domain.mentor.entity.Mentor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MentorResponseDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Basic {
        private Long mentorId;
        //private Long userId;
        //private String userName;
        private Long jobId;
        private String jobName;
        private String name;
        private String currentCompany;
        private Integer career;
        private String phone;
        private String introduction;
        private Mentor.ApprovalStatus isApproved;
        private Mentor.ApprovalStatus isModified;
        private String bestFor;

        // Entity를 DTO로 변환하는 정적 메서드
        public static Basic from(Mentor entity) {
            return Basic.builder()
                    .mentorId(entity.getId())
                    //.userId(entity.getUser().getUserId())
                    //.userName(entity.getUser().getNickname())
                    .jobId(entity.getJob().getId())
                    .jobName(entity.getJob().getName())
                    .name(entity.getName())
                    .currentCompany(entity.getCurrentCompany())
                    .career(entity.getCareer())
                    .phone(entity.getPhone())
                    .introduction(entity.getIntroduction())
                    .isApproved(entity.getIsApproved())
                    .isModified(entity.getIsModified())
                    .bestFor(entity.getBestFor())
                    .build();
        }
    }
}
