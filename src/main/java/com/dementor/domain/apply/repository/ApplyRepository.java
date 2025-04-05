package com.dementor.domain.apply.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dementor.domain.apply.entity.Apply;

import java.util.Optional;

public interface ApplyRepository extends JpaRepository<Apply, Long> {

	Page<Apply> findByMemberId(Long memberId, Pageable pageable);



//	// <chat> 멘토링 클래스 ID (classId)와 신청자 ID (menteeId)를 조건으로,해당 Apply 객체를 조회
//	//Apply 객체에서 멘티의 Member 꺼내기 위해
//	Optional<Apply> findByMentoringClassIdAndMemberId(Long classId, Long memberId);

}
