package com.dementor.domain.chat.repository;

import com.dementor.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
//    Optional<ChatRoom> findByApplymentId(Long applymentId);
//    //채팅방을 멘토링 신청ID로 조회 - applymentId로 연결된 채팅방이 이미 있는지 확인

    List<ChatRoom> findByMember_Id(Long memberId);
    // 사용자 기준 채팅방 목록조회


    List<ChatRoom> findByAdmin_AdminId(Long adminId);
   // 관리자 기준 채팅방 목록 조회

}