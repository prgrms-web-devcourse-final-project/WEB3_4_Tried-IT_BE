package com.dementor.domain.apply.dto.response;

import com.dementor.domain.apply.entity.Apply;

import com.dementor.domain.chat.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplyIdResponse {
	private Long applyId;
	private Long mentorId;   // 멘토 IDg
	private Long menteeId;   // 멘티 ID
	private Long chatRoomId; // 채팅방 ID

	public static ApplyIdResponse from(Apply apply, ChatRoom room) {
		return ApplyIdResponse.builder()
			.applyId(apply.getId())
			.mentorId(apply.getMentoringClass().getMentor().getId())
			.menteeId(apply.getMember().getId())
			.chatRoomId(room.getChatRoomId())
			.build();
	}
}
