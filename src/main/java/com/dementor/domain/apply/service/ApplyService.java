package com.dementor.domain.apply.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.RoomType;
import com.dementor.domain.chat.service.ChatRoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dementor.domain.apply.dto.request.ApplyCreateRequest;
import com.dementor.domain.apply.dto.response.ApplyIdResponse;
import com.dementor.domain.apply.dto.response.ApplyPageResponse;
import com.dementor.domain.apply.dto.response.ApplyScheduleResponse;
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
import com.dementor.domain.mentoringclass.exception.MentoringClassException;
import com.dementor.domain.mentoringclass.exception.MentoringClassExceptionCode;
import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplyService {

	private final ApplyRepository applyRepository;
	private final MentoringClassRepository mentoringClassRepository;
	private final MemberRepository memberRepository;

	private final ChatRoomService chatRoomService; //


	//멘토링 신청
	@Transactional
	public ApplyIdResponse createApply(ApplyCreateRequest req, Long memberId) {

		MentoringClass mentoringClass = mentoringClassRepository.findById(req.getClassId())
			.orElseThrow(() -> new MentoringClassException(MentoringClassExceptionCode.MENTORING_CLASS_NOT_FOUND));

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		//자신의 멘토링 클래스에 신청할 수 없음
		if (mentoringClass.getMentor().getId().equals(memberId)) {
			throw new ApplyException(ApplyErrorCode.CAN_NOT_APPLY_YOUR_CLASS);
		}

		//일정 필수
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



		//---------------챗 영역---------------------
		//  멘토, 멘티 memberId 추출
		Member mentor = mentoringClass.getMember();
		Member mentee = apply.getMember();


		// 멘토링 채팅방 생성
		chatRoomService.getOrCreateMentoringChatRoom(
				mentor.getId(),
				mentee.getId(),
				mentor.getNickname() // 초기 멘티가 볼 닉네임(동적조회로 각각 상대방 닉네임 조회 가능)
		);

//		return ApplyResponse.GetApplyId.from(savedApply);
		return ApplyIdResponse.from(savedApply);
	}

	//멘토링 신청 취소
	@Transactional
	public void deleteApply(Long applyId, Long memberId) {

		Apply apply = applyRepository.findById(applyId)
			.orElseThrow(() -> new ApplyException(ApplyErrorCode.APPLY_NOT_FOUND));

		if (!apply.getMember().getId().equals(memberId)) {
			throw new ApplyException(ApplyErrorCode.NOT_YOUR_APPLY);
		}

		applyRepository.delete(apply);

	}

	//내가 신청한 멘토링 목록 조회 (페이징)
	public ApplyPageResponse getApplyList(Long memberId, int page, int size) {
		memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		Pageable pageable = PageRequest.of(page, size);

		Page<Apply> applyPage = applyRepository.findByMemberId(memberId, pageable);

		return ApplyPageResponse.from(applyPage, page, size);
	}

	//특정 멘토링 신청 날짜 목록 조회
	public ApplyScheduleResponse getApplySchedulesByClassId(Long classId, String startDate, String endDate) {

		mentoringClassRepository.findById(classId)
			.orElseThrow(() -> new MentoringClassException(MentoringClassExceptionCode.MENTORING_CLASS_NOT_FOUND));

		List<Apply> applies = applyRepository.findAllByClassIdAndScheduleBetween(
			classId, startDate, endDate);

		return ApplyScheduleResponse.fromList(applies);
	}
}