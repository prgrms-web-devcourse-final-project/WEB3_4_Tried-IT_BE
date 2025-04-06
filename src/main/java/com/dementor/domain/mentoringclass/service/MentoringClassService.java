package com.dementor.domain.mentoringclass.service;

import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentoringclass.dto.request.MentoringClassCreateRequest;
import com.dementor.domain.mentoringclass.dto.request.MentoringClassUpdateRequest;
import com.dementor.domain.mentoringclass.dto.request.ScheduleRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentoringClassService {
    /*
    * TODO : 예외처리 계층화, 공통 로직 분리(페이징 끝), 엔티티 수정
    *
    * */

    private final MentoringClassRepository mentoringClassRepository;
    private final ScheduleRepository scheduleRepository;
    private final MentorRepository mentorRepository;

    public Page<MentoringClassFindResponse> findAllClass(Long jobId, Pageable pageable) {
        Page<MentoringClass> mentoringClasses;
        if (jobId != null)
            mentoringClasses = mentoringClassRepository.findByMentor_Job_Id(jobId, pageable);
        else
            mentoringClasses = mentoringClassRepository.findAll(pageable);

        return mentoringClasses.map(MentoringClassFindResponse::from);
    }

    @Transactional
    public MentoringClassDetailResponse createClass(Long mentorId, MentoringClassCreateRequest request) {
        Mentor mentor = mentorRepository.findById(mentorId)
            .orElseThrow(() -> new MentoringClassException("멘토를 찾을 수 없습니다: " + mentorId));

        // 입력값 검증
        if(request.title() == null || request.content() == null)
            throw new MentoringClassException(MentoringClassExceptionCode.TITLE_OR_CONTENT_INPUT_NULL.getMessage());
        else if(request.price() < 0)
            throw new MentoringClassException(MentoringClassExceptionCode.MINUS_PRICE.getMessage());
        else if(request.schedules() == null)
            throw new MentoringClassException(MentoringClassExceptionCode.EMPTY_SCHEDULE.getMessage());
        else if(request.stack() == null)
            throw new MentoringClassException(MentoringClassExceptionCode.EMPTY_STACK.getMessage());

        MentoringClass mentoringClass = MentoringClass.builder()
                .title(request.title())
                .stack(String.join(",", request.stack()))
                .content(request.content())
                .price(request.price())
                .mentor(mentor)
                .build();
        
        mentoringClass = mentoringClassRepository.save(mentoringClass);

        // 스케줄 저장 로직 별도로 관리
        List<Schedule> schedules = createSchedules(request.schedules(), mentoringClass);
        mentoringClass.updateSchedules(schedules);
        
        return MentoringClassDetailResponse.from(mentoringClass);
    }

    private List<Schedule> createSchedules(List<ScheduleRequest> scheduleRequests, MentoringClass mentoringClass) {
        return scheduleRequests.stream()
                .map(scheduleRequest -> {
                    Schedule schedule = Schedule.builder()
                            .dayOfWeek(scheduleRequest.dayOfWeek())
                            .time(scheduleRequest.time())
                            .mentoringClass(mentoringClass)
                            .build();
                    return scheduleRepository.save(schedule);
                })
                .collect(Collectors.toList());
    }

    public MentoringClassDetailResponse findOneClass(Long classId) {
        MentoringClass mentoringClass = mentoringClassRepository.findById(classId)
                .orElseThrow(() -> new MentoringClassException(MentoringClassExceptionCode.MENTORING_CLASS_NOT_FOUND.getMessage()));
        return MentoringClassDetailResponse.from(mentoringClass);
    }

    public void deleteClass(Long classId) {
        mentoringClassRepository.deleteById(classId);
    }

    @Transactional
    public MentoringClassUpdateResponse updateClass(Long classId, Long memberId, MentoringClassUpdateRequest request) {
        MentoringClass mentoringClass = mentoringClassRepository.findById(classId)
                .orElseThrow(() -> new MentoringClassException(MentoringClassExceptionCode.MENTORING_CLASS_NOT_FOUND.getMessage()));
        
        if (!mentoringClass.getMentor().getId().equals(memberId))
            throw new MentoringClassException(MentoringClassExceptionCode.MENTORING_CLASS_UNAUTHORIZED.getMessage());

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
        if (request.schedule() != null) {
            Schedule schedule = mentoringClass.getSchedules().get(0);
            schedule.updateDayOfWeek(request.schedule().dayOfWeek());
            schedule.updateTime(request.schedule().time());
            scheduleRepository.save(schedule);
        }

        mentoringClassRepository.save(mentoringClass);

        return new MentoringClassUpdateResponse(
            mentoringClass.getId(),
            new MentoringClassUpdateResponse.MentorInfo(
                mentoringClass.getMentor().getId(),
                mentoringClass.getMentor().getName(),
                mentoringClass.getMentor().getJob().getName(),
                mentoringClass.getMentor().getCareer()
            ),
            mentoringClass.getStack().split(","),
            mentoringClass.getContent(),
            mentoringClass.getTitle(),
            mentoringClass.getPrice(),
            new MentoringClassUpdateResponse.ScheduleInfo(
                mentoringClass.getSchedules().get(0).getDayOfWeek(),
                mentoringClass.getSchedules().get(0).getTime()
            )
        );
    }

}
