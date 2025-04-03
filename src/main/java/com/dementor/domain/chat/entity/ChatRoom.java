package com.dementor.domain.chat.entity;

import com.dementor.domain.admin.entity.Admin;
import com.dementor.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
//
//    @Column
//    private Long applymentId;  // 멘토링 신청 ID

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatMessage> messages = new ArrayList<>();
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime lastMessageAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType; // MENTORING_CHAT, ADMIN_CHAT


    //  연관관계 설정으로 Member 엔티티 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 관리자(admin_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    // 상대방 닉네임 추가
    @Column(name = "target_nickname")
    private String targetNickname;


    // memberId 가져오기
    public Long getMemberId() {
        return member != null ? member.getId() : null;
    }

    // 닉네임 가져오기 (선택적)
//    public String getMemberNickname() {
//        return member != null ? member.getNickname() : "알 수 없음";
//     }
    }
