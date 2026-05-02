package org.example.backend.config;

import org.example.backend.repository.NewsletterSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    @Bean
    public Job newsletterDigestJob(JobRepository jobRepository, Step newsletterDigestStep) {
        return new JobBuilder("newsletterDigestJob", jobRepository)
                .start(newsletterDigestStep)
                .build();
    }

    @Bean
    public Step newsletterDigestStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            NewsletterSubscriptionRepository newsletterSubscriptionRepository
    ) {
        return new StepBuilder("newsletterDigestStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    long activeSubscribers = newsletterSubscriptionRepository.countByActiveTrue();
                    log.info("[BATCH] Newsletter digest prepared for {} active subscribers", activeSubscribers);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
