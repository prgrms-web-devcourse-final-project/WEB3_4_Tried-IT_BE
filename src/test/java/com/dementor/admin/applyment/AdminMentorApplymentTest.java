package com.dementor.admin.applyment;

import com.dementor.config.TestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
public class AdminMentorApplymentTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private MemberRepository memberRepository;
//
//    @Autowired
//    private MentoringClassRepository mentoringClassRepository;
//
//    @Autowired
//    private MentorApplyProposalRepository MentorApplyProposalRepository;
//
//    @Autowired
//    private ApplyRepository applyRepository;
//
//    @Autowired
//    private MentorRepository mentorRepository;
//
//    @Autowired
//    private JobRepository jobRepository;
//
//    private Member testMentee;
//    private Member testMentor;
//    private Long testMentoringClassId;
//    private MentoringClass testMentoringClass;
//    private CustomUserDetails menteePrincipal;
//    private CustomUserDetails mentorPrincipal;
//
//    @BeforeEach
//    void setUp() {
//        // 테스트용 멘토 지원서 생성
//        MentorApplyProposal MentorApplyProposal1 = MentorApplyProposal.builder()
//                .name("테스트멘토1")
//                .email("mentor1@test.com")
//                .phone("01012345678")
//                .career(3)
//                .currentCompany("테스트회사1")
//                .introduction("자기소개1")
//                .bestFor("이런 분야가 특기입니다1")
//                .build();
//
//        MentorApplyProposal MentorApplyProposal2 = MentorApplyProposal.builder()
//                .name("테스트멘토2")
//                .email("mentor2@test.com")
//                .phone("01087654321")
//                .career(5)
//                .currentCompany("테스트회사2")
//                .introduction("자기소개2")
//                .bestFor("이런 분야가 특기입니다2")
//                .build();
//
//        // 멘토 지원서 저장
//        MentorApplyProposalRepository.save(MentorApplyProposal1);
//        MentorApplyProposalRepository.save(MentorApplyProposal2);
//
//    }
//
//    @Test
//    @DisplayName("멘토 지원 전체 조회 성공 테스트")
//    void findAllAdminMentorApplymentSuccess() throws Exception {
//        // when & then
//        mockMvc.perform(get("/api/admin/mentor/applyment")
//                        .param("page", "0")
//                        .param("size", "10")
//                        .param("sortBy", "createdAt")
//                        .param("order", "DESC")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.isSuccess").value(true))
//                .andExpect(jsonPath("$.code").value("200"))
//                .andExpect(jsonPath("$.message").value("멘토 지원 전체 조회 성공"))
//                .andExpect(jsonPath("$.data.content").isArray())
//                .andExpect(jsonPath("$.data.totalElements").isNumber())
//                .andExpect(jsonPath("$.data.totalPages").isNumber())
//                .andExpect(jsonPath("$.data.size").value(10))
//                .andExpect(jsonPath("$.data.number").value(0));
//    }

}
