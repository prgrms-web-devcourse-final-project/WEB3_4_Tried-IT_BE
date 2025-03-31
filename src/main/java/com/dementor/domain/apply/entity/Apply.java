package com.dementor.domain.apply.entity;

import java.time.LocalDateTime;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
public class Apply {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	//문의 내용 (멘토에게 하고 싶은 말)
	@Column(nullable = false)
	private String inquiry;

	//신청 상태
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ApplyStatus applyStatus;


	//멘티가 신청한 날짜
	@Column(nullable = false)
	private LocalDateTime schedule;

	//회원 연관관계
	//@ManyToOne
	//private

	//멘토링 수업 연관관계
	//@ManyToOne
	//private


}
