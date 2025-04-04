package com.dementor.domain.postattachment.repository;

import com.dementor.domain.postattachment.entity.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {
}
