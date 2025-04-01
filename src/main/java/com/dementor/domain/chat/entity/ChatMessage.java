package com.dementor.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Table(name = "chat_message")
@Getter @Setter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;


    @Enumerated(EnumType.STRING) // ENTER, MESSAGE, EXIT
    @Column(nullable = false)
    private MessageType type;


    @Column(nullable = false)
    private Long memberId;

    @Column
    private Long adminId; // 추후 연동 대비

    @Column(nullable = false)
    private String nickname;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private SenderType senderType; // "MEMBER" 또는 "ADMIN"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();




}
