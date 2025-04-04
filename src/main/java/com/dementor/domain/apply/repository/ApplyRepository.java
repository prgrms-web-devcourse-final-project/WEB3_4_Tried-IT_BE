package com.dementor.domain.apply.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dementor.domain.apply.entity.Apply;

public interface ApplyRepository extends JpaRepository<Apply, Long> {

	Page<Apply> findByMemberId(Long memberId, Pageable pageable);

	Page<Apply> findByMentoringClassIdIn(List<Long> classId, Pageable pageable);
}
