package com.dementor.domain.mentorapplyproposal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposal;

@Repository
public interface MentorApplyProposalRepository extends JpaRepository<MentorApplyProposal, Long> {
    //회원 ID로 멘토 지원 내역 조회
    @Query("SELECT ma FROM MentorApplyProposal ma WHERE ma.member.id = :memberId")
    Optional<MentorApplyProposal> findByMemberId(@Param("memberId") Long memberId);

    //회원 ID로 멘토 지원 내역 존재 여부 확인
    @Query("SELECT COUNT(ma) > 0 FROM MentorApplyProposal ma WHERE ma.member.id = :memberId")
    boolean existsByMemberId(@Param("memberId") Long memberId);

    // 가장 최근 지원서 조회 메소드 추가
    @Query("SELECT ma FROM MentorApplyProposal ma WHERE ma.member.id = :memberId ORDER BY ma.createdAt DESC")
    Optional<MentorApplyProposal> findLatestByMemberId(@Param("memberId") Long memberId);
}
