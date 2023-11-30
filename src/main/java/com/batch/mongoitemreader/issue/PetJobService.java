package com.batch.mongoitemreader.issue;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class PetJobService {
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private PetJobConfig petJobConfig;

    @SneakyThrows
    public Long runJob() {
        Job job = petJobConfig.readPetFromMongoJob();
        JobExecution jobExecution = jobLauncher.run(job, new JobParametersBuilder().toJobParameters());
        return jobExecution.getJobId();
    }


}
