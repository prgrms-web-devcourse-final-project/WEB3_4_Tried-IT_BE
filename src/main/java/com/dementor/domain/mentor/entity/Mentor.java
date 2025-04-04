package com.dementor.domain.mentor.entity;

import com.dementor.domain.job.entity.Job;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.mentoringclass.entity.MentoringClass;
import com.dementor.domain.postattachment.entity.PostAttachment;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "mentor")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Mentor {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // member_id를 PK이자 FK로 사용
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    // PostAttachment 엔티티와의 관계 (일대다)
    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostAttachment> attachments;

    // Mentoring 수업 엔티티와의 관계 (일대다)
    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MentoringClass> mentorings;

    @Column(length = 10, nullable = false)
    private String name;

    @Column(length = 20)
    private String currentCompany;

    @Column(nullable = false)
    private Integer career;

    @Column(length = 20, nullable = false)
    private String phone;

    @Column(length = 20, nullable = false)
    private String email;

    @Column(length = 255, nullable = false)
    private String introduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ModificationStatus modificationStatus = ModificationStatus.NONE;

    @Column(length = 255)
    private String bestFor;

    // 승인 상태 Enum
    public enum ApprovalStatus {
        PENDING,    // 대기 중
        APPROVED,   // 승인됨
        REJECTED    // 거부됨
    }

    // 정보 수정 상태 Enum
    public enum ModificationStatus {
        NONE,      // 수정 요청 없음
        PENDING,   // 승인 대기 중
        APPROVED,  // 수정 승인됨
        REJECTED   // 수정 거부됨
    }

    // 승인 상태 변경 메서드
    public void updateApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    // 정보 수정 상태 변경 메서드
    public void updateModificationStatus(ModificationStatus modificationStatus) {
        this.modificationStatus = modificationStatus;
    }

    // 첨부파일 목록 업데이트 메서드
    public void updateAttachments(List<PostAttachment> attachments) {
        this.attachments = attachments;
    }

    // 필드 수정 메서드
    public void update(String currentCompany, Integer career, String phone,
                       String email, String introduction, String bestFor) {
        this.currentCompany = currentCompany;
        this.career = career;
        this.phone = phone;
        this.email = email;
        this.introduction = introduction;
        this.bestFor = bestFor;
        this.modificationStatus = ModificationStatus.PENDING;
    }
}
