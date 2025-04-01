package com.dementor.domain.chat.repository;

import com.dementor.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
//    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
//    //전체 메시지를 오래된 순으로 가져옴-> 커서기반이면 굳이 필요없는듯

    List<ChatMessage> findTop20ByChatRoom_ChatRoomIdAndChatMessageIdLessThanOrderByChatMessageIdDesc(Long chatRoomId, Long messageId);
    // 커서 기반 페이징 -이전메시지 20개 가져오기

    List<ChatMessage> findTop20ByChatRoom_ChatRoomIdOrderByChatMessageIdDesc(Long chatRoomId);
    //채팅방 처음 들어올때 20개조회

    List<ChatMessage> findTop1ByChatRoom_ChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);
    // 채팅방 목록 마지막 메시지용 - 가장 최근 메시지 1개

}
