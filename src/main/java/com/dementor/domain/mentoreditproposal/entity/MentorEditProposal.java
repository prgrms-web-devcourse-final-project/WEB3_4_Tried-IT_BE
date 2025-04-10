package com.dementor.domain.mentoreditproposal.entity;

import com.dementor.domain.job.entity.Job;
import com.dementor.domain.member.entity.Member;
import com.dementor.global.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mentor_edit_proposal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class MentorEditProposal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false)
    private Integer career;

    @Column(length = 20)
    private String currentCompany;

    @Column(nullable = false)
    private String introduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MentorEditProposalStatus status;

    //List<PostAttachment> attachments; // 첨부파일

    // 수정 요청 상태 업데이트
    public void updateStatus(MentorEditProposalStatus status) {
        this.status = status;
    }
}
