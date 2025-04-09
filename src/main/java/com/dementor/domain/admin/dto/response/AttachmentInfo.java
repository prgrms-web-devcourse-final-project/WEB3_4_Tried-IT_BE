package com.dementor.domain.admin.dto.response;

import com.dementor.domain.postattachment.entity.PostAttachment;

public record AttachmentInfo(
        Long attachmentId,
        String fileName,
        String fileUrl
) {
    public static AttachmentInfo from(PostAttachment attachment) {
        return new AttachmentInfo(
                attachment.getId(),
                attachment.getFilename(),
                attachment.getStoreFilePath()
        );
    }
}
