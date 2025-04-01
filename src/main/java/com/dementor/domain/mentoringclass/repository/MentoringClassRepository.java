package com.dementor.domain.mentoringclass.repository;

import com.dementor.domain.mentoringclass.entity.MentoringClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentoringClassRepository extends JpaRepository<MentoringClass, Long> {
    List<MentoringClass> findByMentor_Job_Id(Long jobId);
}
