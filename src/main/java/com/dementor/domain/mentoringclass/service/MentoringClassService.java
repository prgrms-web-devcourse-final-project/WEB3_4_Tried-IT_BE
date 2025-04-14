package com.dementor.domain.mentoringclass.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dementor.domain.mentor.dto.response.MyMentoringResponse;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentoringclass.dto.request.MentoringClassCreateRequest;
import com.dementor.domain.mentoringclass.dto.request.MentoringClassUpdateRequest;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassDetailResponse;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassFindResponse;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassUpdateResponse;
import com.dementor.domain.mentoringclass.entity.MentoringClass;
import com.dementor.domain.mentoringclass.entity.Schedule;
import com.dementor.domain.mentoringclass.exception.MentoringClassException;
import com.dementor.domain.mentoringclass.exception.MentoringClassExceptionCode;
import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;
import com.dementor.domain.mentoringclass.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MentoringClassService {

	private final MentoringClassRepository mentoringClassRepository;
	private final ScheduleRepository scheduleRepository;
	private final MentorRepository mentorRepository;

	public Page<MentoringClassFindResponse> findAllClass(List<Long> jobId, Pageable pageable) {
		Page<MentoringClass> mentoringClasses;

		if (jobId == null || jobId.isEmpty()) {// Job id가 없으면
			mentoringClasses = mentoringClassRepository.findAll(pageable);
		} else if (jobId.size() == 1) { // Job id가 하나만 입력되면
			mentoringClasses = mentoringClassRepository.findByMentor_Job_Id(jobId.get(0), pageable);
		} else { // job Id가 여러개면
			mentoringClasses = mentoringClassRepository.findByMentor_Job_IdIn(jobId, pageable);
		}

		return mentoringClasses.map(MentoringClassFindResponse::from);
	}

	@Transactional
	public MentoringClassDetailResponse createClass(Long mentorId, MentoringClassCreateRequest request) {
		Mentor mentor = mentorRepository.findById(mentorId)
			.orElseThrow(() -> new IllegalArgumentException("멘토를 찾을 수 없습니다.")); // TODO : 멘토 예외처리를 가져와야 함

		// 입력값 검증
		if (request.title() == null || request.content() == null)
			throw new MentoringClassException(MentoringClassExceptionCode.TITLE_OR_CONTENT_INPUT_NULL);
		else if (request.price() < 0)
			throw new MentoringClassException(MentoringClassExceptionCode.MINUS_PRICE);
		else if (request.schedules() == null)
			throw new MentoringClassException(MentoringClassExceptionCode.EMPTY_SCHEDULE);
		else if (request.stack() == null)
			throw new MentoringClassException(MentoringClassExceptionCode.EMPTY_STACK);

		MentoringClass mentoringClass = MentoringClass.builder()
			.title(request.title())
			.stack(String.join(",", request.stack()))
			.content(request.content())
			.price(request.price())
			.mentor(mentor)
			.build();
		mentoringClass = mentoringClassRepository.save(mentoringClass);

		// 스케줄 저장 로직 별도로 관리
		MentoringClass savedMentoringClass = mentoringClass;
		List<Schedule> schedules = request.schedules().stream()
			.map(scheduleRequest -> Schedule.builder()
				.mentoringClassId(savedMentoringClass.getId())
				.dayOfWeek(scheduleRequest.dayOfWeek())
				.time(scheduleRequest.time())
				.build())
			.map(scheduleRepository::save)
			.toList();

		return MentoringClassDetailResponse.from(mentoringClass, schedules);
	}

	public MentoringClassDetailResponse findOneClass(Long classId) {
		// 멘토링 클래스 정보 조회
		MentoringClass mentoringClass = mentoringClassRepository.findById(classId)
			.orElseThrow(() -> new MentoringClassException(MentoringClassExceptionCode.MENTORING_CLASS_NOT_FOUND));
		// 조회한 클래스 id로 스케줄 정보 조회
		List<Schedule> schedules = scheduleRepository.findByMentoringClassId(classId);
		// 같이 response
		return MentoringClassDetailResponse.from(mentoringClass, schedules);
	}

	public void deleteClass(Long classId) {
		mentoringClassRepository.deleteById(classId);
	}

	@Transactional
	public MentoringClassUpdateResponse updateClass(Long classId, Long memberId, MentoringClassUpdateRequest request) {
		MentoringClass mentoringClass = mentoringClassRepository.findById(classId)
			.orElseThrow(() -> new MentoringClassException(MentoringClassExceptionCode.MENTORING_CLASS_NOT_FOUND));

		if (!mentoringClass.getMentor().getId().equals(memberId))
			throw new MentoringClassException(MentoringClassExceptionCode.MENTORING_CLASS_UNAUTHORIZED);

		// 일정 아닌 정보
		if (request.title() != null)
			mentoringClass.updateTitle(request.title());
		if (request.content() != null)
			mentoringClass.updateContent(request.content());
		if (request.price() != null)
			mentoringClass.updatePrice(request.price());
		if (request.stack() != null)
			mentoringClass.updateStack(request.stack());

		// 일정 정보
		Schedule schedule = scheduleRepository.findByMentoringClassId(classId)
			.stream()
			.findFirst()
			.orElseThrow(() -> new MentoringClassException(MentoringClassExceptionCode.SCHEDULE_NOT_FOUND));

		if (request.schedule() != null) {
			schedule.updateDayOfWeek(request.schedule().dayOfWeek());
			schedule.updateTime(request.schedule().time());
		}

		return new MentoringClassUpdateResponse(
			mentoringClass.getId(),
			new MentoringClassUpdateResponse.MentorInfo(
				mentoringClass.getMentor().getId(),
				mentoringClass.getMentor().getName(),
				mentoringClass.getMentor().getJob().getName(),
				mentoringClass.getMentor().getCareer()
			),
			mentoringClass.getStack(),
			mentoringClass.getContent(),
			mentoringClass.getTitle(),
			mentoringClass.getPrice(),
			new MentoringClassUpdateResponse.ScheduleInfo(
				schedule.getDayOfWeek(),
				schedule.getTime()
			)
		);
	}

	public List<MyMentoringResponse> getMentorClassFromMentor(Long memberId) {
		List<MentoringClass> mentoringList = mentoringClassRepository.findByMentor_Id(memberId);

		return mentoringList.stream()
			.map(mentoringClass -> new MyMentoringResponse(
				mentoringClass.getId(),
				mentoringClass.getStack(),
				mentoringClass.getContent(),
				mentoringClass.getTitle(),
				mentoringClass.getPrice()
			))
			.collect(Collectors.toList());
	}

}
