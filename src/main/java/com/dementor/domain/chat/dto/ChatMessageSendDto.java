package com.dementor.domain.chat.dto;

import com.dementor.domain.chat.entity.MessageType;
import com.dementor.domain.chat.entity.SenderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageSendDto {  // 클라이언트->서버

//    private MessageType type;  // ENTER / MESSAGE / EXIT
    private Long chatRoomId;
    private SenderType senderType;
    private Long senderId;
    private String content;    // 메시지 본문


}
