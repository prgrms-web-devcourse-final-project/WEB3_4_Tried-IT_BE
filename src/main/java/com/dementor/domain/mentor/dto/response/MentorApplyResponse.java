package com.dementor.domain.mentor.dto.response;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.entity.ApplyStatus;

import lombok.Builder;
import lombok.Getter;

public class MentorApplyResponse {

	@Getter
	@Builder
	public static class ApplyMenteeDto {
		private Long applyId;
		private Long classId;
		private Long memberId;     // 멘티 ID
		private String nickname;   // 멘티 닉네임
		private ApplyStatus status;
		private String inquiry;
		private ZonedDateTime schedule;

		public static ApplyMenteeDto from(Apply apply) {
			return ApplyMenteeDto.builder()
				.applyId(apply.getId())
				.classId(apply.getMentoringClass().getId())
				.memberId(apply.getMember().getId())
				.nickname(apply.getMember().getNickname())
				.status(apply.getApplyStatus())
				.inquiry(apply.getInquiry())
				.schedule(apply.getSchedule().atZone(ZoneId.systemDefault()))
				.build();
		}
	}

	@Getter
	@Builder
	public static class Pagination {
		private int page;
		private int size;
		private long total_elements;
		private int total_pages;
	}

	@Getter
	@Builder
	public static class GetApplyMenteePageList {
		private List<ApplyMenteeDto> applyments;
		private Pagination pagination;

		public static GetApplyMenteePageList from(Page<Apply> page, int pageNum, int size) {
			return GetApplyMenteePageList.builder()
				.applyments(page.map(ApplyMenteeDto::from).getContent())
				.pagination(Pagination.builder()
					.page(pageNum + 1)
					.size(size)
					.total_elements(page.getTotalElements())
					.total_pages(page.getTotalPages())
					.build())
				.build();
		}
	}
}
