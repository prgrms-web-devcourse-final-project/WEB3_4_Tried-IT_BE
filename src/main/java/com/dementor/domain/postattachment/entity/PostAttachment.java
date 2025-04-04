package com.dementor.domain.postattachment.entity;

import com.dementor.domain.member.entity.Member;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_attachment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename; // 서버에 저장된 파일명 (UUID 등 사용)

    @Column(nullable = false)
    private String originalFilename; //원본 파일명

    @Column(nullable = false)
    private String storeFilePath;

    @Column(nullable = false)
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private Mentor mentor;

    // 파일이 마크다운 내 이미지인지 일반 첨부파일인지 구분
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType imageType;

    // 예: 마크다운에서 ![alt](/images/abc123.png) 형태로 참조할 때 abc123이 uniqueIdentifier . UUID 형식 추천
    @Column(unique = true)
    private String uniqueIdentifier; // 마크다운 내 이미지 참조를 위한 고유 식별자

    @Builder
    public PostAttachment(String filename, String originalFilename, String storeFilePath,
                          Long fileSize, Member member, Mentor mentor, ImageType imageType) {
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.storeFilePath = storeFilePath;
        this.fileSize = fileSize;
        this.member = member;
        this.mentor = mentor;
        this.imageType = imageType != null ? imageType : ImageType.NORMAL;
    }

    // ImageType enum
    public enum ImageType {
        NORMAL,      // 일반 첨부 파일
        MARKDOWN_SELF_INTRODUCTION,     // "나를 소개하는 글" 마크다운 내 이미지
        MARKDOWN_RECOMMENDATION, // 추천 대상 마크다운 내 이미지
    }
}
