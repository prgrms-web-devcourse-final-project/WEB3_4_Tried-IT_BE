package com.dementor.domain.mentoringclass.repository;

import com.dementor.domain.mentoringclass.entity.MentoringClass;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MentoringClassRepository extends JpaRepository<MentoringClass, Long> {
	// JPA 에서 IN은 하나라도 포함하는 데이터를 출력하는데, 지금은 모두 포함하는 데이터가 필요해서 JPQL을 사용함.
	@Query("SELECT mc FROM MentoringClass mc WHERE mc.mentor.job.id IN :jobId")
	Page<MentoringClass> findByMentor_Job_IdIn(
		@Param("jobId") List<Long> jobId,
		Pageable pageable
	);

	Page<MentoringClass> findByMentor_Job_Id(Long mentor_job_id, Pageable pageable);

	List<MentoringClass> findByMentor_Id(Long mentorId);
}
