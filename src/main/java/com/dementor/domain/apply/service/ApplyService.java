package com.dementor.domain.apply.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dementor.domain.apply.dto.request.ApplyRequest;
import com.dementor.domain.apply.dto.response.ApplyResponse;
import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.entity.ApplyStatus;
import com.dementor.domain.apply.exception.ApplyErrorCode;
import com.dementor.domain.apply.exception.ApplyException;
import com.dementor.domain.apply.repository.ApplyRepository;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.exception.MemberErrorCode;
import com.dementor.domain.member.exception.MemberException;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentoringclass.entity.MentoringClass;
import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplyService {

	private final ApplyRepository applyRepository;
	private final MentoringClassRepository mentoringClassRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public ApplyResponse.GetApplyId createApply(ApplyRequest.ApplyCreateRequest req, Long memberId) {

		MentoringClass mentoringClass = mentoringClassRepository.findById(req.getClassId())
			.orElseThrow(() -> new IllegalArgumentException("멘토링 클래스를 찾을 수 없습니다."));

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		if (req.getSchedule() == null) {
			throw new ApplyException(ApplyErrorCode.SCHEDULE_REQUIRED);
		}

		Apply apply = Apply.builder()
			.mentoringClass(mentoringClass)
			.inquiry(req.getInquiry())
			.applyStatus(ApplyStatus.PENDING)
			.schedule(req.getSchedule())
			.member(member)
			.build();

		Apply savedApply = applyRepository.save(apply);

		return ApplyResponse.GetApplyId.from(savedApply);
	}
}