package com.dementor.domain.mentor.entity;

import com.dementor.domain.job.entity.Job;
import com.dementor.domain.member.entity.Member;
import com.dementor.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mentor_application")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MentorApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(length = 10, nullable = false)
    private String name;

    @Column(name = "current_company", length = 20)
    private String currentCompany;

    @Column(nullable = false)
    private Integer career;

    @Column(length = 20, nullable = false)
    private String phone;

    @Column(length = 20, nullable = false)
    private String email;

    @Column(length = 255, nullable = false)
    private String introduction;

    @Column(name = "best_for", length = 255)
    private String bestFor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private Member reviewer;

    public enum ApplicationStatus {
        PENDING, APPROVED, REJECTED
    }

    // 지원 상태 업데이트
    public void updateStatus(ApplicationStatus status, String rejectionReason) {
        this.status = status;
    }

    // 멘토 엔티티로 변환
    public Mentor toMentor() {
        if (this.status != ApplicationStatus.APPROVED) {
            throw new IllegalStateException("승인되지 않은 지원은 멘토로 변환할 수 없습니다.");
        }

        return Mentor.builder()
                .member(this.member)
                .job(this.job)
                .name(this.name)
                .career(this.career)
                .phone(this.phone)
                .email(this.email)
                .currentCompany(this.currentCompany)
                .introduction(this.introduction)
                .bestFor(this.bestFor)
                .approvalStatus(Mentor.ApprovalStatus.APPROVED)
                .build();
    }
}
