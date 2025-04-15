package com.dementor.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "chat_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long chatRoomId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RoomType roomType; // MENTORING_CHAT, ADMIN_CHAT

	@Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private ZonedDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = ZonedDateTime.now();  // 서버 시스템 시간대 기준
		}
	}


	@Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private ZonedDateTime lastMessageAt;
	// 메시지 보낼 때 직접 갱신
	public void updateLastMessageTime(ZonedDateTime sentAt) {
		this.lastMessageAt = sentAt;
	}

	// 관리자 채팅용
	@Column(name = "admin_id")
	private Long adminId;

	@Column(name = "member_id")
	private Long memberId;

	// 멘토링 채팅용 (1:1 기준)
	@Column(name = "mentor_id")
	private Long mentorId;

	@Column(name = "mentee_id")
	private Long menteeId;

	//    @Column(name = "target_nickname")
	//    private String targetNickname;
}
