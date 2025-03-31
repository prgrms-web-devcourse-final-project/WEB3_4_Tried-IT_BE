package com.dementor.domain.apply.dto.response;

import java.time.LocalDateTime;

import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.entity.ApplyStatus;

import lombok.Builder;
import lombok.Getter;



public class ApplyResponse {

	@Getter
	@Builder
	public static class GetApplyList { // 신청 목록
		private Long applyment_id;
		private Long class_id;
		private Long mentor_id;
		private String name; //멘토 이름
		private ApplyStatus status;
		private String inquiry;
		private String year_month; // 년, 월
		private LocalDateTime schedule; //신청 날짜 2025-03-25T14:00
		private Integer page; //페이지
		private Integer size; //사이즈
		private Long totalElements; //총 요소
		private Integer totalPages; //총 페이지

		public static GetApplyList from(Apply apply) {
			return GetApplyList.builder()
				.applyment_id(apply.getId())
				//.class_id(apply.getClassId())
				//.mentor_id(apply.getMentorId())
				//.name(.getname()) //멘토 이름
				.status(apply.getApplyStatus())
				.inquiry(apply.getInquiry())
				//.year_month// 신청 날짜에서 년, 월만
				.schedule(apply.getSchedule())//신청 날짜
				.build();
		}
	}

	@Getter
	@Builder
	public static class GetStatus{ // 승인, 거절
		private Long applyment_id;
		private Long class_id;
		private ApplyStatus status;

		public static GetStatus from(Apply apply) {
			return GetStatus.builder()
				.applyment_id(apply.getId())
				//.class_id(apply.getClassId())
				.status(apply.getApplyStatus())
				.build();
		}
	}

	@Getter
	@Builder
	public static class GetApplyMenteeList{
		private Long applyment_id;
		private Long class_id;
		private Long user_id;
		private String nickname; //멘티 닉네임 (회원)
		private ApplyStatus status;
		private String inquiry;
		private String year_month; // 년, 월
		private LocalDateTime schedule; //신청 날짜 2025-03-25T14:00
		private Integer page;
		private Integer size;
		private Long totalElements;
		private Integer totalPages;

		public static GetApplyMenteeList from(Apply apply) {
			return GetApplyMenteeList.builder()
				.applyment_id(apply.getId())
				//.class_id(apply.getClassId())
				//.user_id(apply.getMentorId())
				//.name(.getnickname()) //회원 닉네임
				.status(apply.getApplyStatus())
				.inquiry(apply.getInquiry())
				//.year_month// 년, 월
				.schedule(apply.getSchedule())//신청 날짜
				.build();
		}
	}


}
