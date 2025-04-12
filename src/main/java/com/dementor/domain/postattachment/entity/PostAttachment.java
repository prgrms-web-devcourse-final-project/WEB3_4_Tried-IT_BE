package com.dementor.domain.postattachment.entity;

import com.dementor.domain.member.entity.Member;
import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposal;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposal;
import com.dementor.global.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

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
	@JoinColumn(name = "mentor_apply_proposal_id")
	private MentorApplyProposal mentorApplyProposal;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mentor_modification_id")
	private MentorEditProposal mentorEditProposal;

	@Column(unique = true)
	private String uniqueIdentifier; // 마크다운 내 이미지 참조를 위한 고유 식별자

	// 멤버 조회 메서드 추가
	public Member getMember() {
		if (this.mentorApplyProposal != null) {
			return this.mentorApplyProposal.getMember();
		} else if (this.mentorEditProposal != null) {
			return this.mentorEditProposal.getMember();
		}
		return null;
	}
}
