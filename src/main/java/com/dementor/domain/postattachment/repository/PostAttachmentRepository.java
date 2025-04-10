package com.dementor.domain.postattachment.repository;

import com.dementor.domain.postattachment.entity.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {
    // 멤버 ID로 파일 조회
    List<PostAttachment> findByMemberId(Long memberId);

    // 고유 식별자로 파일 조회 (마크다운 이미지용)
    Optional<PostAttachment> findByUniqueIdentifier(String uniqueIdentifier);

    // 멤버별 파일 수 카운트 (제한 체크용)
    long countByMemberId(Long memberId);
}
