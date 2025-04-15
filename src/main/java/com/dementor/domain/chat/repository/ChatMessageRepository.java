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

	// 내가 보지 않은 메시지가 있는지 여부 (뱃지 표시용)
	boolean existsByChatRoom_ChatRoomIdAndSenderIdNotAndReadFalse(Long chatRoomId, Long viewerId);

	//  내가 보지 않은 메시지들을  가져오는 메서드 (read = false를 true로 바꿀때 사용)
	List<ChatMessage> findByChatRoom_ChatRoomIdAndSenderIdNotAndReadFalse(Long chatRoomId, Long viewerId);

}
