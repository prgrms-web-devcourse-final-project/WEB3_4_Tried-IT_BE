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
import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;
import com.dementor.domain.mentoringclass.repository.ScheduleRepository;
import com.dementor.domain.mentoringclass.dto.SortDirection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentoringClassService {
    private final MentoringClassRepository mentoringClassRepository;
    private final ScheduleRepository scheduleRepository;
    private final MentorRepository mentorRepository;

    public Page<MentoringClassFindResponse> findAllClass(Long jobId, int page, int size, String sortBy, SortDirection order) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.valueOf(order.name()), sortBy));

        Page<MentoringClass> mentoringClasses;
        if (jobId != null)
            mentoringClasses = mentoringClassRepository.findByMentor_Job_Id(jobId, pageRequest);
        else
            mentoringClasses = mentoringClassRepository.findAll(pageRequest);

        return mentoringClasses.map(MentoringClassFindResponse::from);
    }

    @Transactional
    public MentoringClassDetailResponse createClass(Long mentorId, MentoringClassCreateRequest request) {
        Mentor mentor = mentorRepository.findById(mentorId)
            .orElseThrow(() -> new IllegalArgumentException("멘토를 찾을 수 없습니다: " + mentorId));

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
        mentoringClass.setSchedules(schedules);
        
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
                .orElseThrow(() -> new IllegalArgumentException("멘토링 수업를 찾을 수 없습니다: " + classId));
        return MentoringClassDetailResponse.from(mentoringClass);
    }

    public void deleteClass(Long classId) {
        mentoringClassRepository.deleteById(classId);
    }

    @Transactional
    public MentoringClassUpdateResponse updateClass(Long classId, Long memberId, MentoringClassUpdateRequest request) {
        MentoringClass mentoringClass = mentoringClassRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멘토링 클래스입니다."));
        
        if (!mentoringClass.getMentor().getId().equals(memberId))
            throw new IllegalArgumentException("해당 멘토링 클래스를 수정할 권한이 없습니다.");

        // 일정 아닌 정보
        if (request.title() != null)
            mentoringClass.updateTitle(request.title());
        if (request.content() != null)
            mentoringClass.updateDescription(request.content());
        if (request.price() != null)
            mentoringClass.updatePrice(request.price());
        if (request.stack() != null)
            mentoringClass.setStack(String.join(",", request.stack()));

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
