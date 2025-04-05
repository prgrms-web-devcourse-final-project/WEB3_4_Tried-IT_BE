package com.dementor.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatRoomId;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime lastMessageAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType; // MENTORING_CHAT, ADMIN_CHAT

    // 관리자 채팅용
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "member_id")
    private Long memberId;

    // 멘토링 채팅용 (1:1 기준)
    @Column(name = "mentor_id")
    private Long mentorId;

    @Column(name = "mentee_id")
    private Long menteeId;

    @Column(name = "target_nickname")
    private String targetNickname;
}
