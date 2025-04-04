package com.dementor.domain.mentor.controller;

import com.dementor.domain.mentor.dto.response.MyMentoringResponse;
import com.dementor.domain.mentor.service.MentorServiceFromHo;
import com.dementor.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MentorControllerFromHo {
    private final MentorServiceFromHo mentorService;

    @GetMapping("/api/mentor/class/{menberId}")
    public ApiResponse<?> getMentorClassFromMentor(
            @PathVariable Long menberId
    ) {
        MyMentoringResponse response = mentorService.getMentorClassFromMentor(menberId);
        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "멘토링 수업 조회 성공",
                response
        );
    }
}
