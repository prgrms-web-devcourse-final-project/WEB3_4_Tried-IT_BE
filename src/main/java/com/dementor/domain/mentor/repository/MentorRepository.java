package com.dementor.domain.mentor.repository;

import com.dementor.domain.mentor.entity.Mentor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MentorRepository extends JpaRepository<Mentor, Long> {
	@Query("SELECT mc.id FROM MentoringClass mc WHERE mc.mentor = :mentor")
	List<Long> findMentoringClassIdsByMentor(@Param("mentor") Mentor mentor);

	Optional<Mentor> findByMemberId(Long memberId);
}
