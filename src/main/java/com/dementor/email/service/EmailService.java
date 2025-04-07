package com.dementor.email.service;

import java.time.Duration;
import java.util.Random;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.dementor.domain.member.exception.MemberErrorCode;
import com.dementor.domain.member.exception.MemberException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;
	private final RedisTemplate<String, String> redisTemplate;
	private static final long EXPIRE_TIME = 10 * 60; // 5분

	// 랜덤 인증번호 생성
	private String createCode() {
		Random random = new Random();
		return String.format("%06d", random.nextInt(1000000));
	}

	//TODO : 예외 처리 필요
	public void sendVerificationEmail(String email) throws MessagingException {
		String code = createCode();

		redisTemplate.opsForValue().set("email:" + email, code, Duration.ofSeconds(EXPIRE_TIME));

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		String subject = "[DeMentor] 이메일 인증 코드";
		String content = """
                <div style="font-family: Arial, sans-serif; text-align: center;">
                    <h2>이메일 인증 코드</h2>
                    <p>아래 인증 코드를 입력하여 이메일 인증을 완료하세요.</p>
                    <h1 style="color: #007bff;">%s</h1>
                    <p>이 인증 코드는 10분 후 만료됩니다.</p>
                    <p>감사합니다.<br>DeMentor 팀 드림</p>
                </div>
                """.formatted(code);

		helper.setTo(email);
		helper.setSubject(subject);
		helper.setText(content, true); // HTML 적용

		mailSender.send(message);
	}

	public boolean verifyCode(String email, String inputCode){
		String storedCode = redisTemplate.opsForValue().get("email:" + email);
		if (storedCode != null && storedCode.equals(inputCode)) {
			//redisTemplate.delete("email:" + email); 회원가입 시 인증코드로 검증 과정 필요
			return true;
		}else{
			throw new MemberException(MemberErrorCode.INVALID_VERIFICATION_CODE);
		}

	}

}
