package com.dementor.domain.mentor.entity;

import com.dementor.domain.member.entity.Member;
import com.dementor.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mentor_modification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentorModification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "changes", nullable = false, columnDefinition = "TEXT")
    private String changes; // JSON 형태로 변경 내용 저장

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModificationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private Member reviewer;

    public enum ModificationStatus {
        PENDING, APPROVED, REJECTED
    }

    @Builder
    public MentorModification(Member member, String changes, ModificationStatus status) {
        this.member = member;
        this.changes = changes;
        this.status = status != null ? status : ModificationStatus.PENDING;
    }

    // 수정 요청 상태 업데이트
    public void updateStatus(ModificationStatus status, String rejectionReason) {
        this.status = status;
    }
}
