package com.dementor.domain.member.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "내 정보 조회")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberInfoResponse {
	@Schema(description = "member_id")
	private Long id;

	@Schema(description = "email")
	private String email;

	@Schema(description = "nickname")
	private String nickname;

	@Schema(description = "created_at")
	private LocalDateTime created_at;
}
