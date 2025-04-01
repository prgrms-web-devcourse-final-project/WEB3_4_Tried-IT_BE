package com.dementor.domain.apply.dto.response;

import java.time.ZonedDateTime;

import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.entity.ApplyStatus;

import lombok.Builder;
import lombok.Getter;



public class ApplyResponse {

	@Getter
	@Builder
	public static class GetApplyId{
		private Long applyment_id;

		public static GetApplyId from(Apply apply) {
			return GetApplyId.builder()
				.applyment_id(apply.getId())
				.build();
		}
	}


	@Getter
	@Builder
	public static class GetApplyList { // 신청 목록
		private Long applymentId;
		private Long classId;
		private Long memberId; //멘토 아이디
		private String name; //멘토 이름
		private ApplyStatus status;
		private String inquiry;
		private ZonedDateTime schedule; //신청 날짜 2025-03-25T14:00
		private Integer page; //페이지
		private Integer size; //사이즈
		private Long totalElements; //총 요소
		private Integer totalPages; //총 페이지

		public static GetApplyList from(Apply apply) {
			return GetApplyList.builder()
				.applymentId(apply.getId())
				.classId(apply.getMentoringClass().getId())
				.memberId(apply.getMember().getId())
				.name(apply.getMember().getNickname())
				.status(apply.getApplyStatus())
				.inquiry(apply.getInquiry())
				.schedule(ZonedDateTime.from(apply.getSchedule()))//신청 날짜
				.build();
		}
	}

	@Getter
	@Builder
	public static class GetApplyMemberList{
		private Long applymentId;
		private Long classId;
		private Long memberId; //멘티
		private String nickname; //멘티 닉네임 (회원)
		private ApplyStatus status;
		private String inquiry;
		private ZonedDateTime schedule; //신청 날짜 2025-03-24T14:30+09:00
		private Integer page;
		private Integer size;
		private Long totalElements;
		private Integer totalPages;

		public static GetApplyMemberList from(Apply apply) {
			return GetApplyMemberList.builder()
				.applymentId(apply.getId())
				.classId(apply.getMentoringClass().getId())
				.memberId(apply.getMember().getId())
				.nickname(apply.getMember().getNickname()) //회원 닉네임
				.status(apply.getApplyStatus())
				.inquiry(apply.getInquiry())
				.schedule(ZonedDateTime.from(apply.getSchedule()))//신청 날짜
				.build();
		}
	}

	@Getter
	@Builder
	public static class GetStatus{ // 승인, 거절
		private Long applymentId;
		private Long classId;
		private ApplyStatus status;

		public static GetStatus from(Apply apply) {
			return GetStatus.builder()
				.applymentId(apply.getId())
				.classId(apply.getMentoringClass().getId())
				.status(apply.getApplyStatus())
				.build();
		}
	}



}