package com.dementor.global;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;

@Component
@Profile("local") // 로컬 환경에서만 실행되도록
public class TestDataInit implements CommandLineRunner {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public TestDataInit(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		// 기존 데이터가 없을 때만 생성
		if (memberRepository.count() == 0) {
			Member testMentor = Member.builder()
				.email("mentor@test.com")
				.password(passwordEncoder.encode("1234"))
				.nickname("테스트멘토")
				.name("TEST_NAME")
				.userRole(UserRole.MENTOR)
				.build();

			Member testMentee = Member.builder()
				.email("mentee@test.com")
				.password(passwordEncoder.encode("1234"))
				.nickname("테스트멘티")
				.name("TEST_NAME")
				.userRole(UserRole.MENTEE)
				.build();

			memberRepository.save(testMentor);
			memberRepository.save(testMentee);
		}
	}
}