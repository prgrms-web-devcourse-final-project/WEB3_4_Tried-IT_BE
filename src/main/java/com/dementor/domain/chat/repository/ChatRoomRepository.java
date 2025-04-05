package com.dementor.domain.chat.repository;

import com.dementor.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 멤버가 참여한 모든 멘토링 채팅방 조회 (멘토든 멘티든 상관없이)
    @Query("""
        SELECT r FROM ChatRoom r
        WHERE r.roomType = 'MENTORING_CHAT'
          AND (:memberId IN (r.mentorId, r.menteeId))
    """)
    List<ChatRoom> findMentoringChatRoomsByMemberId(@Param("memberId") Long memberId);

    // 멤버가 참여한 관리자 채팅방 조회
    @Query("""
        SELECT r FROM ChatRoom r
        WHERE r.roomType = 'ADMIN_CHAT'
          AND r.memberId = :memberId
    """)
    List<ChatRoom> findAdminChatRoomsByMemberId(@Param("memberId") Long memberId);

    // 관리자가 참여한 관리자 채팅방 조회
    @Query("""
        SELECT r FROM ChatRoom r
        WHERE r.roomType = 'ADMIN_CHAT'
          AND r.adminId = :adminId
    """)
    List<ChatRoom> findAdminChatRoomsByAdminId(@Param("adminId") Long adminId);
}
