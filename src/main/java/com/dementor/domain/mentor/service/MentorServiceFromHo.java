package com.dementor.domain.mentor.service;

import com.dementor.domain.mentor.dto.response.MyMentoringResponse;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.repository.MentorRepoFromHo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorServiceFromHo {
    private final MentorRepoFromHo mentorRepoFromHo;

    public List<MyMentoringResponse> getMentorClassFromMentor(Long memberId) {
        List<Mentor> mentors = mentorRepoFromHo.findByMemberId(memberId);
        
        return mentors.stream()
                .flatMap(mentor -> mentor.getMentorings().stream())
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
