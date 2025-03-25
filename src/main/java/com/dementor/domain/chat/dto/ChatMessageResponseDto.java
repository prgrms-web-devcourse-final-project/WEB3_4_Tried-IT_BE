package com.dementor.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponseDto {  //구독자에게 브로드캐스트할 출력 DTO (Receive용)
                                        // 서버->구독자

    private String type;         // MESSAGE, ENTER, EXIT
    private Long applymentId;
    private Long userId;
    private String nickname;
    private String message;
    private ZonedDateTime sentAt;  //locaDate타입도 가능.
}

// 브로드캐스트 sub/chat/room/{id}