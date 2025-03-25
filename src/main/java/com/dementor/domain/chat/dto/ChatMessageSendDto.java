package com.dementor.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageSendDto {  //입력 DTO(send용) 클라이언트->서버

    public enum MessageType {
        ENTER, MESSAGE, EXIT
    }

    private MessageType type;
    private Long applymentId;  // 어떤 채팅방인지
    private String message;    // 메시지 본문
}
