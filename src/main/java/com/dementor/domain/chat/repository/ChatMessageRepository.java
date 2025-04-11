package com.dementor.domain.chat.repository;

import com.dementor.domain.chat.entity.ChatMessage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	//    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
	//    //전체 메시지를 오래된 순으로 가져옴-> 커서기반이면 굳이 필요없는듯

	// 채팅방 처음 입장 시 → 최신 20개 메시지 조회 (chatMessageId 기준 내림차순)
	List<ChatMessage> findTop20ByChatRoom_ChatRoomIdOrderByChatMessageIdDesc(Long chatRoomId);

	// 과거 메시지 조회 (이전 메시지 ID 기준, 내림차순)
	List<ChatMessage> findTop20ByChatRoom_ChatRoomIdAndChatMessageIdLessThanOrderByChatMessageIdDesc(Long chatRoomId,
		Long chatMessageId);

	// 채팅방 목록 조회용: 해당 방의 마지막 메시지 1개 (보통 가장 최근 메시지 시간 확인용)
	List<ChatMessage> findTop1ByChatRoom_ChatRoomIdOrderBySentAtDesc(Long chatRoomId);
}
