package com.dementor.domain.member.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dementor.domain.member.dto.request.SignupRequest;
import com.dementor.domain.member.dto.response.MemberInfoResponse;
import com.dementor.domain.member.service.MemberService;
import com.dementor.email.service.EmailService;
import com.dementor.global.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Tag(name = "회원 관리", description = "회원가입, 이메일 인증, 중복 체크, 유저 정보 조회")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	private final EmailService emailService;

	@PostMapping()
	public ApiResponse<?> createMember(@RequestBody SignupRequest signupRequest) {
		memberService.createMember(signupRequest);
		return ApiResponse.of(true, HttpStatus.CREATED , "Create member");
	}

	@GetMapping("/isEmail")
	public ApiResponse<Boolean> isEmail(@RequestParam("email") String email) {
		boolean isEmail = memberService.isEmail(email);
		return ApiResponse.of(true, HttpStatus.OK, "Email exists", isEmail);
	}

	@GetMapping("/isNickname")
	public ApiResponse<Boolean> isNickname(@RequestParam("nickname") String nickname) {
		boolean isNickname = memberService.isNickname(nickname);
		return ApiResponse.of(true, HttpStatus.OK, "Nickname exists", isNickname);
	}

	@PostMapping("/verifyCode")
	public ApiResponse<?> sendVerificationEmail(@RequestParam("email") String email) throws MessagingException {
		emailService.sendVerificationEmail(email);
		return ApiResponse.of(true, HttpStatus.OK, "Send verification code");
	}

	@GetMapping("/verifyEmail")
	public ApiResponse<Boolean> verifyEmailCode(@RequestParam("email") String email, @RequestParam("verifyCode") String verifyCode){
		boolean verified = emailService.verifyCode(email, verifyCode);
		return ApiResponse.of(true, HttpStatus.OK, "verified Email", verified);
	}

	@GetMapping("/info")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<MemberInfoResponse> info(Principal principal) {
		MemberInfoResponse memberInfo = memberService.getMemberInfo(principal.getName());
		return ApiResponse.of(true, HttpStatus.OK, "Member Info", memberInfo);
	}

}
