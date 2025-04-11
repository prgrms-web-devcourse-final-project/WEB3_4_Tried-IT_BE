package com.dementor.domain.mentoringclass.dto.request;

public record MentoringClassModifyRequest(
	Long classId,
	String stack,
	String content,
	String title,
	int price,
	Long schedule
) {
}
