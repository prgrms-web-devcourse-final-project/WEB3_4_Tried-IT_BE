package com.dementor.job.controller;

import com.dementor.config.TestSecurityConfig;
import com.dementor.domain.job.dto.request.JobCreaeteRequest;
import com.dementor.domain.job.dto.request.JobUpdateRequest;
import com.dementor.domain.job.entity.Job;
import com.dementor.domain.job.repository.JobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
public class JobControllerTest { // TODO : fixture monkey 전부 적용하면 낙관적 락 에러가 뜸. 지금은 하나만 적용했는데, 나중에 해결해야 함
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private FixtureMonkey fixtureMonkey;

    @BeforeEach
    void setUp() {
        fixtureMonkey = FixtureMonkey.builder()
                .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
                .build();

        Job backendJob = Job.builder()
                .name("백엔드 개발자")
                .build();
        jobRepository.save(backendJob);

        Job frontendJob = Job.builder()
                .name("프론트엔드 개발자")
                .build();
        jobRepository.save(frontendJob);

        Job devopsJob = Job.builder()
                .name("DevOps 엔지니어")
                .build();
        jobRepository.save(devopsJob);
    }

    @Test
    void findAllJobs() throws Exception {
        // given
        // when & then
        mockMvc.perform(get("/api/admin/job")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("직무 전체 조회 성공"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void createJob() throws Exception {
        // given
        JobCreaeteRequest request = new JobCreaeteRequest("C++ 개발자");

        // when & then
        mockMvc.perform(post("/api/admin/job")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.message").value("직무 생성 성공"))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    void updateJob() throws Exception {
        // given
        Job job = jobRepository.findAll().get(0);
        JobUpdateRequest request = fixtureMonkey.giveMeBuilder(JobUpdateRequest.class)
                .set("jobName", "업데이트된 직무명")
                .sample();

        // when & then
        mockMvc.perform(put("/api/admin/job/{id}", job.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("직무 정보 수정 성공"));
    }

    @Test
    void deleteJob() throws Exception {
        // given
        Job job = jobRepository.findAll().get(0);

        // when & then
        mockMvc.perform(delete("/api/admin/job/{id}", job.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("직무 삭제 성공"));
    }
}
