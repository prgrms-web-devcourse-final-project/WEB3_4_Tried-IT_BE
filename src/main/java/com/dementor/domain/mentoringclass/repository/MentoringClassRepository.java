package com.dementor.domain.mentoringclass.repository;

import com.dementor.domain.mentoringclass.entity.MentoringClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentoringClassRepository extends JpaRepository<MentoringClass, Long> {
    Page<MentoringClass> findByMentor_Job_Id(Long jobId, Pageable pageable);
}
