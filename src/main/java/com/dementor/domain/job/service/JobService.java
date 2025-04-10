package com.dementor.domain.job.service;

import com.dementor.domain.job.dto.request.JobCreaeteRequest;
import com.dementor.domain.job.dto.request.JobUpdateRequest;
import com.dementor.domain.job.dto.response.JobCreateResponse;
import com.dementor.domain.job.dto.response.JobFindResponse;
import com.dementor.domain.job.dto.response.JobUpdateResponse;
import com.dementor.domain.job.entity.Job;
import com.dementor.domain.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;

    public List<JobFindResponse> getJobList() {
        return jobRepository.findAll()
                .stream()
                .map(Job -> new JobFindResponse(
                        Job.getId(),
                        Job.getName()))
                .toList();
    }

    public JobCreateResponse createJob(JobCreaeteRequest request) {
        Job job = Job.builder()
                .name(request.jobName())
                .build();
        job = jobRepository.save(job);

        return JobCreateResponse.of(job.getId(), request.jobName());
    }

    public JobUpdateResponse updateJob(Long jobId, JobUpdateRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("직무를 찾을 수 없습니다: " + jobId));

        if (job.getName() != null) {
            job.updateName(request.getJobName());
            jobRepository.save(job);
        }

        return new JobUpdateResponse(job.getName());
    }

    public void deleteJob(Long jobId) {
        jobRepository.deleteById(jobId);
    }
}
