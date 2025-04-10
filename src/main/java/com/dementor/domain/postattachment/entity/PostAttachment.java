package com.dementor.domain.postattachment.entity;

import com.dementor.domain.member.entity.Member;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposal;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposal;
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
import lombok.Setter;

@Entity
@Table(name = "post_attachment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename; // 서버에 저장된 파일명 (UUID 등 사용)

    @Column(nullable = false)
    private String originalFilename; //원본 파일명

    @Column(length = 1024, nullable = false)
    private String storeFilePath;

    @Column(nullable = false)
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private Mentor mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_apply_proposal_id")
    private MentorApplyProposal MentorApplyProposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_modification_id")
    private MentorEditProposal mentorEditProposal;

    // 파일이 마크다운 내 이미지인지 일반 첨부파일인지 구분
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType imageType;

    // 예: 마크다운에서 ![alt](/images/abc123.png) 형태로 참조할 때 abc123이 uniqueIdentifier . UUID 형식 추천
    @Column(unique = true)
    private String uniqueIdentifier; // 마크다운 내 이미지 참조를 위한 고유 식별자

    // ImageType enum
    public enum ImageType {
        NORMAL,      // 일반 첨부 파일
        MARKDOWN_SELF_INTRODUCTION,     // "나를 소개하는 글" 마크다운 내 이미지
    }

    // 멘토 지원서와 연결
    public void connectToMentorApplyProposal(MentorApplyProposal application) {
        this.MentorApplyProposal = application;
    }

    // 멘토 정보 수정 요청과 연결
    public void connectToMentorModification(MentorEditProposal modification) {
        this.mentorEditProposal = modification;
    }
}
