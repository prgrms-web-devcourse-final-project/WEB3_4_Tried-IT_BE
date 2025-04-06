package com.dementor.member.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dementor.domain.member.dto.request.SignupRequest;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.exception.MemberErrorCode;
import com.dementor.domain.member.exception.MemberException;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.member.service.MemberService;
import com.dementor.email.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MemberControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private MemberRepository memberRepository;

	@MockitoBean
	private MemberService memberService;

	@MockitoBean
	private EmailService emailService;

	@BeforeEach
	void setUp() {
		memberRepository.save(
			Member.builder()
				.email("mentee@test.com")
				.nickname("testMentee")
				.name("TEST_NAME")
				.password(passwordEncoder.encode("1234")) // 실제 테스트에서는 암호화된 값 사용 추천
				.userRole(UserRole.MENTEE) // ENUM 타입
				.build()
		);
	}


	@Test
	@DisplayName("이메일 중복확인 - 사용 가능한 이메일")
	public void testIsEmailSuccess() throws Exception {
		// given
		String email = "test@example.com";
		when(memberService.isEmail(email)).thenReturn(true);

		// when & then
		mvc.perform(get("/api/members/isEmail")
				.param("email", email))
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("Email exists"))
			.andExpect(jsonPath("$.data").value(true));
	}

	@Test
	@DisplayName("이메일 중복확인 - 이미 존재하는 이메일")
	public void testIsEmailDuplicate() throws Exception {
		// given
		String email = "mentee@test.com";
		when(memberService.isEmail(email))
			.thenThrow(new MemberException(MemberErrorCode.DUPLICATE_EMAIL));

		// when & then
		mvc.perform(get("/api/members/isEmail")
				.param("email", email))
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("409"))
			.andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다"));
	}

	@Test
	@DisplayName("닉네임 중복확인 - 사용 가능한 닉네임")
	public void testIsNicknameSuccess() throws Exception {
		// given
		String nickname = "hong";
		when(memberService.isNickname(nickname)).thenReturn(true);

		// when & then
		mvc.perform(get("/api/members/isNickname")
				.param("nickname", nickname))
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("Nickname exists"))
			.andExpect(jsonPath("$.data").value(true));
	}

	@Test
	@DisplayName("이메일 중복확인 - 이미 존재하는 이메일")
	public void testIsNicknameDuplicate() throws Exception {
		// given
		String nickname = "testMentee";
		when(memberService.isNickname(nickname))
			.thenThrow(new MemberException(MemberErrorCode.DUPLICATE_NICKNAME));

		// when & then
		mvc.perform(get("/api/members/isNickname")
				.param("nickname", nickname))
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("409"))
			.andExpect(jsonPath("$.message").value("이미 존재하는 닉네임입니다"));
	}


	//TODO : 인증코드 전송 실패 코드 작성
	@Test
	@DisplayName("이메일 인증코드 전송 성공")
	public void testSendVerificationEmailSuccess() throws Exception {
		// given
		String email = "test@email.com";
		doNothing().when(emailService).sendVerificationEmail(email);

		// when & then
		mvc.perform(post("/api/members/verifyCode")
				.param("email", email))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("Send verification code"));
	}


	@Test
	@DisplayName("이메일 인증코드 검증 - redis 에서 성공")
	void testVerifyEmailCodeSuccess() throws Exception {
		// given
		String email = "test@email.com";
		String code = "123456";

		when(emailService.verifyCode(email, code)).thenReturn(true);
		when(emailService.verifyCode(email, "wrong")).thenReturn(false);

		// when & then
		mvc.perform(get("/api/members/verifyEmail")
				.param("email", email)
				.param("verifyCode", code))
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("verified Email"))
			.andExpect(jsonPath("$.data").value(true));
	}

	@Test
	@DisplayName("이메일 인증코드 검증 - 만료된 코드")
	void testVerifyEmailCodeExpired() throws Exception {
		// given
		String email = "test@email.com";
		String code = "123456";

		when(emailService.verifyCode(email, code)).thenReturn(false);

		// when & then
		mvc.perform(get("/api/members/verifyEmail")
				.param("email", email)
				.param("verifyCode", code))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("verified Email"))
			.andExpect(jsonPath("$.data").value(false));
	}

	@Test
	@DisplayName("이메일 인증코드 검증 - 잘못된 코드")
	void testVerifyEmailCodeWrong() throws Exception {
		// given
		String email = "test@email.com";
		String wrongCode = "000000";


		// when & then
		mvc.perform(get("/api/members/verifyEmail")
				.param("email", email)
				.param("verifyCode", wrongCode))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("verified Email"))
			.andExpect(jsonPath("$.data").value(false));
	}

	@Test
	@DisplayName("회원가입 성공")
	void testCreateMemberSuccess() throws Exception {
		// given
		SignupRequest request = SignupRequest.builder()
			.email("test@email.com")
			.password("password123")
			.nickname("nickname")
			.name("name")
			.verifyCode("123456")
			.build();

		doNothing().when(memberService).createMember(any(SignupRequest.class));

		// when & then
		mvc.perform(post("/api/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("201"))
			.andExpect(jsonPath("$.message").value("Create member"));

		verify(memberService).createMember(any(SignupRequest.class));
	}

	@Test
	@DisplayName("회원가입 실패 - 잘못된 코드")
	void testCreateMemberFail() throws Exception {
		// given
		SignupRequest request = SignupRequest.builder()
			.email("test@email.com")
			.password("password123")
			.nickname("nickname")
			.name("name")
			.verifyCode("wrong")
			.build();

		doThrow(new MemberException(MemberErrorCode.INVALID_VERIFYCODE))
			.when(memberService).createMember(any(SignupRequest.class));

		// when & then
		mvc.perform(post("/api/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("400"))
			.andExpect(jsonPath("$.message").value("인증번호가 유효하지 않습니다."));
	}
}

