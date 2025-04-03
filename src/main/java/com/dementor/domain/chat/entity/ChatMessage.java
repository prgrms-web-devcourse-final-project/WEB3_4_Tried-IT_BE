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


//    @Enumerated(EnumType.STRING) // ENTER, MESSAGE, EXIT
//    @Column(nullable = false)
//    private MessageType type;

    // 메시지 타입: ENTER, MESSAGE, EXIT
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    // 발신자 ID (member 또는 admin)
    @Column(nullable = false)
    private Long senderId;

    // 발신자 유형: MEMBER / ADMIN
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SenderType senderType;

    //메시지 본문
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 생성일시
    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

//   -> *senderId로
//    @Column(nullable = false)
//    private Long memberId;
//
//    @Column
//    private Long adminId; // 추후 연동 대비


//    @Column(nullable = false)
//    private String nickname;   - *닉네임 동적으로 붙일것
//

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private SenderType senderType; // "MEMBER" 또는 "ADMIN"







}
