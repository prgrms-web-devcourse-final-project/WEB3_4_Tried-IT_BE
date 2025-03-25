package com.dementor.domain.apply.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "tbl_mentoring_apply")
public class Apply {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "applyment_id")
	private Long applymentId; // 신청 ID

	//문의 내용 (멘토에게 하고 싶은 말)
	@Column(nullable = false)
	private String inquery;

	//신청 상태
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ApplyStatus applyStatus;

	@Column(name = "schedule")
	private String schedule; //타입이 변경될 수 있음

	//회원 연관관계
	//@ManyToOne
	//private

	//멘토링 수업 연관관계
	//@ManyToOne
	//private



}
