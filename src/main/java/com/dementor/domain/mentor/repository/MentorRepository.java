package com.dementor.domain.mentor.repository;

import com.dementor.domain.mentor.entity.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorRepository extends JpaRepository<Mentor, Long> {
}
