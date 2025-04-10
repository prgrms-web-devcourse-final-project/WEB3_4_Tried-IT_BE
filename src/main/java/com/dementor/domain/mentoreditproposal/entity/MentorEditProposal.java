package com.dementor.domain.mentoreditproposal.entity;

import com.dementor.domain.member.entity.Member;
import com.dementor.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "changes", nullable = false, columnDefinition = "TEXT")
    private String changes; // JSON 형태로 변경 내용 저장

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MentorEditProposalStatus status;

    // 수정 요청 상태 업데이트
    public void updateStatus(MentorEditProposalStatus status) {
        this.status = status;
    }
}
