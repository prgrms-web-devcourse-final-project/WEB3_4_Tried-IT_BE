package com.dementor.domain.chat.dto;


import com.dementor.domain.chat.entity.RoomType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatRoomCreateRequestDto {
    private RoomType roomType;  // MENTORING_CHAT or ADMIN_CHAT
    private Long applymentId;    // MENTORING_CHAT일 경우 필수
    private Long targetMemberId; // 관리자 채팅용(ADMIN_CHAT) 관리자 입장에서 상대방 memberId
}
