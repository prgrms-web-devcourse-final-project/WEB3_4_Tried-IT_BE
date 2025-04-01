package com.dementor.domain.mentoringclass.service;

import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentoringclass.dto.request.MentoringClassCreateRequest;
import com.dementor.domain.mentoringclass.dto.request.ScheduleRequest;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassDetailResponse;
import com.dementor.domain.mentoringclass.dto.response.MentoringClassFindResponse;
import com.dementor.domain.mentoringclass.entity.MentoringClass;
import com.dementor.domain.mentoringclass.entity.Schedule;
import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;
import com.dementor.domain.mentoringclass.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
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
    private final MemberRepository memberRepository;

    public List<MentoringClassFindResponse> findClass(Long jobId) {
        List<MentoringClass> mentoringClasses;
        if (jobId != null)
            mentoringClasses = mentoringClassRepository.findByMentor_Job_Id(jobId);
        else
            mentoringClasses = mentoringClassRepository.findAll();

        return mentoringClasses.stream()
                .map(mentoringClass -> new MentoringClassFindResponse(
                        mentoringClass.getId(),
                        mentoringClass.getStack(),
                        mentoringClass.getContent(),
                        mentoringClass.getTitle(),
                        mentoringClass.getPrice(),
                        mentoringClass.getMentor().getJob().getName()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public Long createClass(Long mentorId, MentoringClassCreateRequest request) {
        // 1. 멘토 정보 조회
        Mentor mentor = mentorRepository.findById(mentorId)
            .orElseThrow(() -> new IllegalArgumentException("멘토를 찾을 수 없습니다: " + mentorId));

        // 2. MentoringClass 엔티티 생성
        MentoringClass mentoringClass = MentoringClass.builder()
                .title(request.title())
                .stack(request.stack())
                .content(request.content())
                .price(request.price())
                .mentor(mentor) // 멘토 정보 연동
                .build();
        
        // 3. MentoringClass 저장
        mentoringClass = mentoringClassRepository.save(mentoringClass);
        
        // 4. Schedule 엔티티들 생성 및 저장
        List<Schedule> schedules = createSchedules(request.schedules(), mentoringClass);
        mentoringClass.setSchedules(schedules);
        
        return mentoringClass.getId();
    }

    // 스케줄 저장 로직 별도로 관리
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
}
