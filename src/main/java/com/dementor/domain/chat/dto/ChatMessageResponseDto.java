package com.dementor.domain.chat.dto;

import com.dementor.domain.chat.entity.MessageType;
//import com.dementor.domain.chat.entity.SenderType;
import com.dementor.domain.chat.entity.SenderType;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponseDto {  //구독자에게 브로드캐스트할 출력 DTO (Receive용)
	// 서버->구독자

	//    private MessageType type;         // ENTER(00님이 입장), MESSAGE(실제 채팅),  EXIT(00님 퇴장)
	private Long chatRoomId;
	private Long senderId;        //보낸 사람ID
	private SenderType senderType; // MEMBER, ADMIN, SYSTEM
	//    private String nickname;
	private String content;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
	private ZonedDateTime sentAt; // ✅ ZonedDateTime으로 변경

}

// 브로드캐스트 sub/chat/room/{id}