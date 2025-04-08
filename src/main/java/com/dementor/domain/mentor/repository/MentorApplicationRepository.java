package com.dementor.domain.mentor.repository;

import com.dementor.domain.mentor.entity.MentorApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MentorApplicationRepository extends JpaRepository<MentorApplication, Long> {
    //회원 ID로 멘토 지원 내역 조회
    @Query("SELECT ma FROM MentorApplication ma WHERE ma.member.id = :memberId")
    Optional<MentorApplication> findByMemberId(@Param("memberId") Long memberId);

    //회원 ID로 멘토 지원 내역 존재 여부 확인
    @Query("SELECT COUNT(ma) > 0 FROM MentorApplication ma WHERE ma.member.id = :memberId")
    boolean existsByMemberId(@Param("memberId") Long memberId);

    // 가장 최근 지원서 조회 메소드 추가
    @Query("SELECT ma FROM MentorApplication ma WHERE ma.member.id = :memberId ORDER BY ma.createdAt DESC")
    Optional<MentorApplication> findLatestByMemberId(@Param("memberId") Long memberId);
}
