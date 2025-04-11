package com.dementor.domain.mentor.entity;

import com.dementor.domain.job.entity.Job;
import com.dementor.domain.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
	@MapsId
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "job_id", nullable = false)
	private Job job;

	@Column(length = 10, nullable = false)
	private String name;

	@Column(length = 20)
	private String currentCompany;

	@Column(nullable = false)
	private Integer career;

	@Column(length = 20, nullable = false)
	private String phone;

	@Column(length = 50, nullable = false)
	@Email
	private String email;

	@Column(nullable = false)
	private String introduction;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private ModificationStatus modificationStatus = ModificationStatus.NONE;

	// 정보 수정 상태 변경 메서드
	public void updateModificationStatus(ModificationStatus modificationStatus) {
		this.modificationStatus = modificationStatus;
	}

	// 필드 수정 메서드
	public void update(String currentCompany, Integer career, Job job, String introduction,
		ModificationStatus modificationStatus) {
		this.currentCompany = currentCompany;
		this.job = job;
		this.career = career;
		this.introduction = introduction;
		this.modificationStatus = modificationStatus;
	}
}
