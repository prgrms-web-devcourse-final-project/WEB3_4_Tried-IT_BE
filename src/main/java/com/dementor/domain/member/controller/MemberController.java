package com.dementor.domain.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dementor.domain.member.service.MemberService;
import com.dementor.email.service.EmailService;
import com.dementor.global.ApiResponse;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/signup")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	private final EmailService emailService;

	@PostMapping
	public ApiResponse<?> createMember() {
		return ApiResponse.of(true, HttpStatus.CREATED , "Create member");
	}

	@GetMapping("/isEmail")
	public ApiResponse<?> isEmail(@RequestParam("email") String email) {
		boolean isEmail = memberService.isEmail(email);
		return ApiResponse.of(true, HttpStatus.OK, "Email exists", isEmail);
	}

	@GetMapping("/isNickname")
	public ApiResponse<?> isNickname(@RequestParam("nickname") String nickname) {
		boolean isNickname = memberService.isNickname(nickname);
		return ApiResponse.of(true, HttpStatus.OK, "Nickname exists", isNickname);
	}

	@PostMapping("/verifyCode")
	public ApiResponse<?> sendVerificationEmail(@RequestParam("email") String email) throws MessagingException {
		emailService.sendVerificationEmail(email);
		return ApiResponse.of(true, HttpStatus.OK, "Send verification code");
	}

	@GetMapping("/verifyEmail")
	public ApiResponse<?> verifyEmailCode(@RequestParam("email") String email, @RequestParam("verifyCode") String verifyCode){
		boolean verified = emailService.verifyCode(email, verifyCode);
		return ApiResponse.of(true, HttpStatus.OK, "verified Email", verified);
	}

}
