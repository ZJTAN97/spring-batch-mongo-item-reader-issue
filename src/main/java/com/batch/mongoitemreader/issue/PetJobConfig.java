package com.batch.mongoitemreader.issue;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class PetJobConfig {

    private final PetRepo petRepo;

    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;

    private final MongoTemplate mongoTemplate;

    @Bean
    public Step readPetFromMongo() {
        return new StepBuilder("petReaderMongo", jobRepository)
                .allowStartIfComplete(true)
                .<PetDomain, PetDomain>chunk(1000, platformTransactionManager)
                .reader(petRepo.petReader())
                .writer(new PetWriter(mongoTemplate))
                .faultTolerant()
                .skipLimit(5)
                .skip(IllegalArgumentException.class)
                .build();
    }

    @Bean
    public Job readPetFromMongoJob() {
        return new JobBuilder("petReaderMongoJob", jobRepository)
                .start(readPetFromMongo())
                .build();
    }

}
