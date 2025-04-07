package com.dementor.domain.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dementor.domain.member.dto.request.SignupRequest;
import com.dementor.domain.member.dto.response.MemberInfoResponse;
import com.dementor.domain.member.service.MemberService;
import com.dementor.email.service.EmailService;
import com.dementor.global.ApiResponse;
import com.dementor.global.security.CustomUserDetails;

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
	public ResponseEntity<ApiResponse<Void>> createMember(@RequestBody SignupRequest signupRequest) {
		memberService.createMember(signupRequest);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.of(true, HttpStatus.CREATED , "Create member"));
	}

	@GetMapping("/isEmail")
	public ResponseEntity<ApiResponse<Boolean>> isEmail(@RequestParam("email") String email) {
		boolean isEmail = memberService.isEmail(email);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(true, HttpStatus.OK, "Email exists", isEmail));
	}

	@GetMapping("/isNickname")
	public ResponseEntity<ApiResponse<Boolean>> isNickname(@RequestParam("nickname") String nickname) {
		boolean isNickname = memberService.isNickname(nickname);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(true, HttpStatus.OK, "Nickname exists", isNickname));
	}

	@PostMapping("/verifyCode")
	public ResponseEntity<ApiResponse<Void>> sendVerificationEmail(@RequestParam("email") String email) throws MessagingException {
		emailService.sendVerificationEmail(email);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(true, HttpStatus.OK, "Send verification code"));
	}

	@GetMapping("/verifyEmail")
	public ResponseEntity<ApiResponse<Boolean>> verifyEmailCode(@RequestParam("email") String email, @RequestParam("verifyCode") String verifyCode){
		boolean verified = emailService.verifyCode(email, verifyCode);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(true, HttpStatus.OK, "verified Email", verified));
	}

	@GetMapping("/info")
	public ResponseEntity<ApiResponse<MemberInfoResponse>> info(Authentication authentication) {
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		String email = userDetails.getUsername();

		MemberInfoResponse memberInfo = memberService.getMemberInfo(email);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(true, HttpStatus.OK, "Member Info", memberInfo));
	}

	@PutMapping("/info")
	public ResponseEntity<ApiResponse<Void>> modifyNickname(Authentication authentication, @RequestParam("nickname") String nickname) {
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		String email = userDetails.getUsername();

		memberService.modifyNickname(email, nickname);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.of(true, HttpStatus.OK, "Modify nickname"));
	}

}
