package com.dementor.domain.mentor.repository;

import com.dementor.domain.mentor.entity.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MentorRepoFromHo extends JpaRepository<Mentor, Long> {

    @Query("SELECT m FROM Mentor m WHERE m.member.id = :memberId")
    List<Mentor> findByMemberId(@Param("memberId") Long memberId);
}
