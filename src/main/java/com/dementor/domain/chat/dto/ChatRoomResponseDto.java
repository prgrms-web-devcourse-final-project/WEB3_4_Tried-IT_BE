package com.dementor.domain.chat.dto;


import com.dementor.domain.chat.entity.RoomType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponseDto {
    private Long chatRoomId;
    private RoomType roomType;     // "MENTORING_CHAT" or "ADMIN_CHAT"
    private String lastMessage;
    private LocalDateTime lastSentAt;
    private String targetNickname;  //상대방 닉네임
}
