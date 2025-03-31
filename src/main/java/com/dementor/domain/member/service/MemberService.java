package com.dementor.domain.member.service;

import com.dementor.domain.member.entity.Member;
//import com.dementor.domain.member.exception.MemberErrorCode;
//import com.dementor.domain.member.exception.MemberException;
import com.dementor.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

//    public String getNicknameById(Long memberId) {
//        return memberRepository.findById(memberId)
//                .map(Member::getNickname)
//                .orElse("알 수 없음");
//        // 또는 예외를 던지려면:
//        // .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));
//    }


//**멤버 서비스에   이부분 추가
// 추가된 닉네임 조회 메서드
    public String getNicknameById(Long memberId) {
        return memberRepository.findById(memberId)
                .map(Member::getNickname)
                .orElse("알 수 없음");

    }
}