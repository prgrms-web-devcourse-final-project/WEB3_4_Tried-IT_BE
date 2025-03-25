package com.dementor.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class ChatMessage {



    @Entity
    @Table(name = "chat_messages")
    @Getter
    @Setter
    @NoArgsConstructor
    public class ChatMessage {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long chatMessageId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "chat_room_id", nullable = false)
        private ChatRoom chatRoom;

        @Column(nullable = false)
        private String sender;

        @Column(nullable = false, columnDefinition = "TEXT")
        private String content;

        @Column(nullable = false)
        private LocalDateTime createdAt = LocalDateTime.now();
    }

}
