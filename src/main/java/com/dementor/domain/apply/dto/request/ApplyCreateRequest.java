package com.dementor.domain.apply.dto.request;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplyCreateRequest {
	private Long classId;
	private String inquiry;
	private LocalDateTime schedule;
}
