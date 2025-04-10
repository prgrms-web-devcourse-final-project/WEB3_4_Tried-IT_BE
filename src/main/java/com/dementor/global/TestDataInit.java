package com.dementor.global;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dementor.domain.admin.entity.Admin;
import com.dementor.domain.admin.repository.AdminRepository;
import com.dementor.domain.job.entity.Job;
import com.dementor.domain.job.repository.JobRepository;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;

import lombok.extern.slf4j.Slf4j;


@Component
@Profile("local") // 로컬 환경에서만 실행되도록
@Slf4j
public class TestDataInit implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JobRepository jobRepository;
    private final MentorRepository mentorRepository;
    private final AdminRepository adminRepository;
    private final MentoringClassRepository mentoringClassRepository;

    public TestDataInit(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JobRepository jobRepository, MentorRepository mentorRepository,
                        AdminRepository adminRepository, MentoringClassRepository mentoringClassRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jobRepository = jobRepository;
        this.mentorRepository = mentorRepository;
        this.adminRepository = adminRepository;
        this.mentoringClassRepository = mentoringClassRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (jobRepository.count() == 1) {
//			Job job = Job.builder()
//					.name("백엔드 개발자")
//					.build();
//			jobRepository.save(job);

            Job job = Job.builder()
                    .name("프론트엔드 개발자")
                    .build();
            jobRepository.save(job);

            Job job2 = Job.builder()
                    .name("안드로이드 개발자")
                    .build();
            jobRepository.save(job2);
        }

        if (memberRepository.count() == 0) {
            Member testMentor = Member.builder()
                    .email("mentor@test.com")
                    .password(passwordEncoder.encode("1234"))
                    .nickname("테스트멘토")
                    .name("TEST_NAME")
                    .userRole(UserRole.MENTOR)
                    .build();

            Member testMentee = Member.builder()
                    .email("mentee@test.com")
                    .password(passwordEncoder.encode("1234"))
                    .nickname("테스트멘티")
                    .name("TEST_NAME")
                    .userRole(UserRole.MENTEE)
                    .build();

            memberRepository.save(testMentor);
            memberRepository.save(testMentee);
        }

        if (mentorRepository.count() == 0) {
            Member testMentor = memberRepository.findByEmail("mentor@test.com")
                    .orElseThrow(() -> new RuntimeException("테스트 멘토 회원을 찾을 수 없습니다"));

            Job job = jobRepository.findByName("백엔드 개발자")
                    .orElseThrow(() -> new RuntimeException("백엔드 개발자 직무를 찾을 수 없습니다"));

            Mentor mentorInfo = Mentor.builder()
                    .member(testMentor)
                    .name(testMentor.getName())
                    .currentCompany("테스트회사")
                    .career(5)
                    .phone("010-1234-5678")
                    .email("mentor@test.com")
                    .introduction("테스트 멘토 소개입니다. 경력 5년차 개발자입니다.")
                    .job(job)
                    .build();

            mentorRepository.save(mentorInfo);
        }

        if (mentorRepository.count() < 1) {
            Member testMentor = memberRepository.findByEmail("tigerrla@naver.com")
                    .orElseThrow(() -> new RuntimeException("테스트 멘토 회원을 찾을 수 없습니다"));

            Job job2 = jobRepository.findByName("프론트엔드 개발자")
                    .orElseThrow(() -> new RuntimeException("백엔드 개발자 직무를 찾을 수 없습니다"));

            Mentor mentorInfo = Mentor.builder()
                    .member(testMentor)
                    .name(testMentor.getName())
                    .currentCompany("토스")
                    .career(5)
                    .phone("010-1234-5678")
                    .email("tigerrla@naver.com")
                    .introduction("어 그래 반갑다.")
                    .job(job2)
                    .build();

            mentorRepository.save(mentorInfo);
        }

        if (adminRepository.count() < 1) {

            Admin admin = Admin.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("1234"))
                    .build();

            adminRepository.save(admin);
        }

        // if (mentoringClassRepository.count() <= 10) {
        //     Mentor mentor = mentorRepository.findByName("테스트 멘토")
        //             .orElseThrow(() -> new RuntimeException("테스트 멘토 회원을 찾을 수 없습니다"));
        //
        //     log.info("멘토 id : {}", mentor.getId());
        //
        //     for (int i = 0; i < 50; i++) {
        //         MentoringClass mentoringClass = MentoringClass.builder()
        //                 .title("테스트 데이터 제목" + i)
        //                 .content("테스트 데이터 내용" + i)
        //                 .price(10000 + i)
        //                 .stack("Spring, Java")
        //                 .mentor(mentor)
        //                 .build();
        //
        //         MentoringClass savedMentoringClass = mentoringClassRepository.save(mentoringClass);
        //         log.info("저장된 클래스 id : {}", savedMentoringClass.getId());
        //     }
        // }
    }
}