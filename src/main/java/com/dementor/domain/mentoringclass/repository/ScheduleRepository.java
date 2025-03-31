package com.dementor.domain.mentoringclass.repository;

import com.dementor.domain.mentoringclass.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
} 