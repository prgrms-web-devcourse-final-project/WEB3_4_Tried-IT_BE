package com.dementor.chat.service;

import com.dementor.domain.member.entity.Member;

import javax.management.relation.Role;
//import com.dementor.domain.member.entity.Role;

public class TestMemberFactory {

    public static Member create(Long id, String nickname) {
        Member member = new Member();
        member.setId(id);
        member.setNickname(nickname);
        return member;
    }

    public static Member createMentor(Long id) {
        return create(id, "테스트멘토");
    }

    public static Member createMentee(Long id) {
        return create(id, "테스트멘티");
    }

    public static Member createAdmin(Long id) {
        return create(id, "관리자");
    }
}