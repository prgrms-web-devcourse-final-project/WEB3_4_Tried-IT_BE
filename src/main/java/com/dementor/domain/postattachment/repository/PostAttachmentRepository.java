package com.dementor.domain.postattachment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dementor.domain.postattachment.entity.PostAttachment;

@Repository
public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {

	List<PostAttachment> findByMentorApplyProposalId(Long proposalId);

	List<PostAttachment> findByMentorEditProposalId(Long proposalId);

	// 고유 식별자로 파일 조회 (마크다운 이미지용)
	Optional<PostAttachment> findByUniqueIdentifier(String uniqueIdentifier);

	// 멤버별 파일 수 카운트 수정 - 조인 쿼리 사용
	@Query("SELECT COUNT(p) FROM PostAttachment p WHERE " +
		"(p.mentorApplyProposal.member.id = :memberId) OR " +
		"(p.mentorEditProposal.member.id = :memberId)")
	long countByMemberId(@Param("memberId") Long memberId);
}
