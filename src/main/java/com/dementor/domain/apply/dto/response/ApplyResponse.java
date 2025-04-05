package com.dementor.domain.apply.dto.response;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.entity.ApplyStatus;

import lombok.Builder;
import lombok.Getter;



public class ApplyResponse {

	@Getter
	@Builder
	public static class GetApplyId{
		private Long applymentId;
		private Long mentorId; //for. chat 멘토,멘티Id 사용
		private Long menteeId;

		public static GetApplyId from(Apply apply) {
			return GetApplyId.builder()
					.applymentId(apply.getId())
					.mentorId(apply.getMentoringClass().getMentor().getId())
					.menteeId(apply.getMember().getId())
					.build();
		}
	}


	@Getter
	@Builder
	public static class GetApplyList {
		private Long applymentId;
		private Long classId;
		private Long mentorId; //멘토 아이디
		private String name; //멘토 이름
		private ApplyStatus status;
		private String inquiry;
		private ZonedDateTime schedule;

		public static GetApplyList from(Apply apply) {
			return GetApplyList.builder()
				.applymentId(apply.getId())
				.classId(apply.getMentoringClass().getId())
				.mentorId(apply.getMentoringClass().getMentor().getId())
				.name(apply.getMentoringClass().getMentor().getName())
				.status(apply.getApplyStatus())
				.inquiry(apply.getInquiry())
				.schedule(apply.getSchedule().atZone(ZoneId.systemDefault()))
				.build();
		}
	}

	@Getter
	@Builder
	public static class GetApplyPageList {
		private List<GetApplyList> applyments;
		private Map<String, Object> pagination;

		public static GetApplyPageList from(Page<Apply> applyPage, int page, int size) {
			List<GetApplyList> applyments = applyPage.getContent().stream()
				.map(GetApplyList::from)
				.collect(Collectors.toList());

			Map<String, Object> pagination = new HashMap<>();
			pagination.put("page", page+1);
			pagination.put("size", size);
			pagination.put("total_elements", applyPage.getTotalElements());
			pagination.put("total_pages", applyPage.getTotalPages());

			return GetApplyPageList.builder()
				.applyments(applyments)
				.pagination(pagination)
				.build();
		}
	}

}