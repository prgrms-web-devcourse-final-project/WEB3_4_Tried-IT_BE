package com.dementor.domain.chat.dto;

import com.dementor.domain.chat.entity.RoomType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponseDto {
	private Long chatRoomId;
	private RoomType roomType;     // "MENTORING_CHAT" or "ADMIN_CHAT"
	private String lastMessage;  // 마지막 메시지 내용
	private ZonedDateTime lastMessageAt; // 마지막 메시지 보낸시간
	private String targetNickname;  //상대방 닉네임
	private Long targetId; //상대방 ID
	private boolean hasUnread; //안 읽은 메시지 여부 표시
}
