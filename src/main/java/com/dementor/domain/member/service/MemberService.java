package com.dementor.domain.member.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dementor.domain.member.dto.request.SignupRequest;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.exception.MemberErrorCode;
import com.dementor.domain.member.exception.MemberException;
import com.dementor.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final RedisTemplate<String, String> redisTemplate;

	private final PasswordEncoder passwordEncoder;

	public void createMember(SignupRequest signupRequest) {

		//redis 에 저장된 code 가져오기
		String storedCode = redisTemplate.opsForValue().get("email:" + signupRequest.getEmail());

		//code 검증
		if(storedCode != null && storedCode.equals(signupRequest.getVerifyCode())) {
			Member member = Member.builder()
				.email(signupRequest.getEmail())
				.password(passwordEncoder.encode(signupRequest.getPassword()))
				.nickname(signupRequest.getNickName())
				.name(signupRequest.getName())
				.userRole(UserRole.MENTOR)
				.build();

			memberRepository.save(member);
		}else{
			throw new MemberException(MemberErrorCode.INVALID_VERIFYCODE);
		}
	}

	public boolean isEmail(String email) {
		memberRepository.findByEmail(email).ifPresent(member -> {
			throw new MemberException(MemberErrorCode.DUPLICATE_EMAIL);
		});
		return true;
	}

	public boolean isNickname(String nickname) {
		memberRepository.findByNickname(nickname).ifPresent(member -> {
			throw new MemberException(MemberErrorCode.DUPLICATE_NICKNAME);
		});
		return true;
	}
}
