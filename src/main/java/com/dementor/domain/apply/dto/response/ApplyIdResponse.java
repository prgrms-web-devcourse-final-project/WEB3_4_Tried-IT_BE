package com.dementor.domain.apply.dto.response;

import com.dementor.domain.apply.entity.Apply;

import com.dementor.domain.chat.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplyIdResponse {
	private Long applyId;
	private Long mentorId; //for. chat 멘토,멘티Id 사용
	private Long menteeId;
	private Long chatRoomId;

	public static ApplyIdResponse from(Apply apply, ChatRoom room) {
		return ApplyIdResponse.builder()
			.applyId(apply.getId())
			.mentorId(apply.getMentoringClass().getMentor().getId())
			.menteeId(apply.getMember().getId())
			.chatRoomId(room.getChatRoomId())
			.build();
	}
}
