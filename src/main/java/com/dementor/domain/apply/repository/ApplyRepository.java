package com.dementor.domain.apply.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dementor.domain.apply.entity.Apply;

public interface ApplyRepository extends JpaRepository<Apply, Long> {

	Optional<Apply> findByMentoringClassIdAndMemberId(Long classId, Long memberId);
}
