package com.dementor.domain.chat.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long chatMessageId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom chatRoom;

	//    @Enumerated(EnumType.STRING) // ENTER, MESSAGE, EXIT
	//    @Column(nullable = false)
	//    private MessageType type;
	//
	//    // 메시지 타입: ENTER, MESSAGE, EXIT
	//    @Enumerated(EnumType.STRING)
	//    @Column(nullable = false)
	//    private MessageType messageType;

	// 발신자 ID (member 또는 admin)
	@Column(nullable = false)
	private Long senderId;

	// 발신자 유형: MEMBER / ADMIN
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SenderType senderType;

	//메시지 본문
	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

//	// 메시지 보낸 시간
//	@Column(nullable = false)
//	private LocalDateTime sentAt;
//
//	@PrePersist
//	protected void onCreate() {
//		if (sentAt == null) {
//			sentAt = LocalDateTime.now();
//		}
//	}
// ✅ ZonedDateTime 적용 엔티티
@Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
private ZonedDateTime sentAt; // ✅ LocalDateTime → ZonedDateTime 변경

	@PrePersist
	protected void onCreate() {
		if (sentAt == null) {
			sentAt = ZonedDateTime.now(); // ✅ Asia/Seoul 생략, JVM 기본 시간대 사용
		}
	}
//	@PrePersist
//	protected void onCreate() {
//		if (sentAt == null) {
//			// sentAt = LocalDateTime.now();
//			sentAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")); // ✅ KST로 초기화
//		}
//	}


	@Column(name = "is_read", nullable = false)
	private boolean read = false;


	//    @Column(nullable = false)
	//    private String nickname;   - *닉네임 동적으로 붙일것
	//

}
