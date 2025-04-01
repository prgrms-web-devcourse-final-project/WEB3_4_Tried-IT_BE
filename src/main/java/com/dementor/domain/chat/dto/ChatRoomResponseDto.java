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
    private Long applymentId;  //null이면 관리자 채팅방
    private RoomType roomType;     // "MENTORING_CHAT" or "ADMIN_CHAT"
    private String nickname;  // 멘토,멘티의 닉네임 & 관리자닉네임 = 관리자
    private String lastMessage;
    private LocalDateTime lastSentAt;
}
