package com.dementor.domain.apply.repository;

import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.entity.ApplyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplyRepository extends JpaRepository<Apply, Long> {

	Page<Apply> findByMemberId(Long memberId, Pageable pageable);

	Page<Apply> findByMentoringClassIdIn(List<Long> classId, Pageable pageable);

	@Query("SELECT a FROM Apply a WHERE a.mentoringClass.id = :classId AND " +
	       "FUNCTION('DATE_FORMAT', a.schedule, '%Y%m%d') >= :startDate AND " +
	       "FUNCTION('DATE_FORMAT', a.schedule, '%Y%m%d') <= :endDate")
	List<Apply> findAllByClassIdAndScheduleBetween(
		@Param("classId") Long classId,
		@Param("startDate") String startDate,
		@Param("endDate") String endDate
	);


	// 대기 중인 요청 수 계산
	Integer countByMentoringClassIdInAndApplyStatus(List<Long> mentoringClassIds, ApplyStatus applyStatus);

	// 완료된 세션 수 계산 - 오늘 날짜보다 이전인 세션 카운트
	@Query("SELECT COUNT(a) FROM Apply a WHERE a.mentoringClass.id IN :classIds " +
			"AND a.applyStatus = 'APPROVED' AND a.schedule < CURRENT_DATE")
	Integer countCompletedSessions(@Param("classIds") List<Long> classIds);
}
