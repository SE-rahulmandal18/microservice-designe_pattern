package com.example.jobms.job.impl;

import com.example.jobms.job.Job;
import com.example.jobms.job.JobRepository;
import com.example.jobms.job.JobService;
import com.example.jobms.job.clients.CompanyClient;
import com.example.jobms.job.clients.ReviewClient;
import com.example.jobms.job.dto.JobDTO;
import com.example.jobms.job.external.Company;
import com.example.jobms.job.external.Review;
import com.example.jobms.job.mapper.JobMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    RestTemplate restTemplate;

    private CompanyClient companyClient;
    private ReviewClient reviewClient;

    int attempt=0;

    public JobServiceImpl(JobRepository jobRepository,
                          CompanyClient companyClient,
                          ReviewClient reviewClient) {
        this.jobRepository = jobRepository;
        this.companyClient = companyClient;
        this.reviewClient = reviewClient;
    }

    @Override
    @CircuitBreaker(name = "companyBreaker", fallbackMethod = "companyBreakerFallback")
//    @Retry(name = "companyBreaker", fallbackMethod = "companyBreakerFallback")
//    @RateLimiter(name = "companyBreaker", fallbackMethod = "companyBreakerFallback")
    public List<JobDTO> getAllJobs() {
        System.out.println("Attempt: "+ ++attempt);
        List<Job> jobs = jobRepository.findAll();
        List<JobDTO> jobDTOS = new ArrayList<>();

//          code for RestTemplate USE CASE
//        RestTemplate restTemplate = new RestTemplate();
//        Company company = restTemplate.getForObject("http://localhost:8081/companies/1", Company.class);
//        System.out.println("COMAPANY : " +company.getName());
//        System.out.println("COMAPANY : " +company.getId());
//        return jobRepository.findAll();

        return jobs.stream().map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<String> companyBreakerFallback(Exception e){
        List<String> list  = new ArrayList<>();
        list.add("Fallback Method Called");
        return list;
    }

    private JobDTO convertToDto(Job job) {

        // used when mapper was not included
//        JobWithCompanyDTO jobWithCompanyDTO = new JobWithCompanyDTO();
//        jobWithCompanyDTO.setJob(job);



//        RestTemplate restTemplate = new RestTemplate();
        // this code is replaced by OPENFEIGN below
//        Company company = restTemplate.getForObject("http://COMPANY-SERVICE:8081/companies/" + job.getCompanyId(),
//                Company.class);

        Company company = companyClient.getCompany(job.getCompanyId());


        // this code is replaced by OPENFEIGN below
//        ResponseEntity<List<Review>> reviewResponse  = restTemplate.exchange("http://REVIEW-SERVICE:8083/reviews?companyId=" + job.getCompanyId(),
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<List<Review>>() {
//                });


        List<Review> reviews = reviewClient.getReviews(job.getCompanyId());

//        List<Review> reviews = reviewResponse.getBody();

        // mapper code
        JobDTO jobDTO = JobMapper.mapToJobWithCompanyDTO(job,company,reviews);
//        jobDTO.setCompany(company);

        return jobDTO;

    }

    @Override
    public void createJob(Job job) {
        jobRepository.save(job);
    }


    @Override
    public JobDTO getJobById(Long id) {
        Job job = jobRepository.findById(id).orElse(null);
        return convertToDto(job);
    }

    @Override
    public boolean deleteJobById(Long id) {
        if (jobRepository.existsById(id)) {
            jobRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateJobById(Long id, Job updatedJob) {
        Optional<Job> jobOptional = jobRepository.findById(id);
        if (jobOptional.isPresent()) {
            Job job = jobOptional.get();
            job.setTitle(updatedJob.getTitle());
            job.setDescription(updatedJob.getDescription());
            job.setMinSalary(updatedJob.getMinSalary());
            job.setMaxSalary(updatedJob.getMaxSalary());
            job.setLocation(updatedJob.getLocation());
            jobRepository.save(job);
            return true;
        }
        return false;
    }
}
