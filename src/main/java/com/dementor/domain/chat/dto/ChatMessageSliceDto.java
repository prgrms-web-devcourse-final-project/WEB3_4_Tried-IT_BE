package com.dementor.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageSliceDto {
	private List<ChatMessageResponseDto> messages;
	private boolean hasMore;
	private Long nextCursor;
}
