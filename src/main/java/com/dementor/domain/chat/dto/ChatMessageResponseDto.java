package com.dementor.domain.chat.dto;

import com.dementor.domain.chat.entity.MessageType;
//import com.dementor.domain.chat.entity.SenderType;
import com.dementor.domain.chat.entity.SenderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponseDto {  //구독자에게 브로드캐스트할 출력 DTO (Receive용)
                                        // 서버->구독자

    private MessageType type;         // ENTER(00님이 입장), MESSAGE(실제 채팅),  EXIT(00님 퇴장)
    private Long chatRoomId;
    private Long senderId;        //보낸 사람ID
    private SenderType senderType; // MEMBER, ADMIN, SYSTEM
    private String nickname;
    private String message;
    private ZonedDateTime sentAt;  // 엔티티에서는 local, db는 타임존 개념없이 DATETIME/TIMESTAMP 타입으로만 저장됨


}

// 브로드캐스트 sub/chat/room/{id}