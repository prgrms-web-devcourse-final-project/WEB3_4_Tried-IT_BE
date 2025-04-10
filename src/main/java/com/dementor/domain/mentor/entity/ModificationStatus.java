package com.dementor.domain.mentor.entity;

// 정보 수정 상태 Enum
public enum ModificationStatus {
	NONE,      // 수정 요청 없음
	PENDING,   // 승인 대기 중
	APPROVED,  // 수정 승인됨
	REJECTED   // 수정 거부됨
}