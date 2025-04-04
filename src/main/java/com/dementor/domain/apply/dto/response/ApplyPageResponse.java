package com.dementor.domain.apply.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import com.dementor.domain.apply.entity.Apply;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplyPageResponse {
	private List<ApplyDetailResponse> applyments;
	private Pagination pagination;

	@Getter
	@Builder
	public static class Pagination {
		private int page;
		private int size;
		private long total_elements;
		private int total_pages;
	}

	public static ApplyPageResponse from(Page<Apply> page, int pageNum, int size) {
		return ApplyPageResponse.builder()
			.applyments(page.map(ApplyDetailResponse::from).getContent())
			.pagination(Pagination.builder()
				.page(pageNum + 1)
				.size(size)
				.total_elements(page.getTotalElements())
				.total_pages(page.getTotalPages())
				.build())
			.build();
	}
}
