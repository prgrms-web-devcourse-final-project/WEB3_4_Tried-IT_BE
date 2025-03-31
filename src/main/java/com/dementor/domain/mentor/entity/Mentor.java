package com.dementor.domain.mentor.entity;

import java.util.List;

import com.dementor.domain.job.entity.Job;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.mentoringclass.entity.MentoringClass;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mentor")
@Getter
@AllArgsConstructor
@Builder
public class Mentor {

    @Id
    @Column(name = "member_id")
    private Long id;

    // Member 엔티티와의 관계 (일대일)
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // member_id를 PK이자 FK로 사용
    @JoinColumn(name = "member_id")
    private Member member;

    // categories 엔티티와의 관계 (다대일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    // PostAttachment 엔티티와의 관계 (일대일, 식별관계)
    //todo: 파일첨부 테이블과 연관관계
    //private PostAttachment attachment;

    // Mentoring 수업 엔티티와의 관계 (일대다)
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "mentoringClass_id")
    private List<MentoringClass> mentorings;

    @Column(length = 10, nullable = false)
    private String name;

    @Column(length = 20)
    private String currentCompany;

    @Column(nullable = false)
    private Integer career;

    @Column(length = 20, nullable = false)
    private String phone;

    @Column(length = 255, nullable = false)
    private String introduction;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus isApproved;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus isModified;

    @Column(name = "best_for", length = 255)
    private String bestFor;

    @Column(name = "attachment_id")
    private Long attachmentId;

	public Mentor() {
	}

	// ApprovalStatus Enum 정의
    public enum ApprovalStatus {
        Y, N
    }


    // 멘토링 수업 추가 메서드
    public void addMentoringClass(MentoringClass mentoringClass) {
        mentorings.add(mentoringClass);
    }
}
