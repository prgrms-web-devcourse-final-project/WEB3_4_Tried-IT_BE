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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType; // MENTORING_CHAT, ADMIN_CHAT



    @Column(nullable = false) // 채팅방 생성 시간
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }


    @Column(nullable = true) //마지막 메시지
    private LocalDateTime lastMessageAt;

    // 메시지 보낼 때 직접 갱신
    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }




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
