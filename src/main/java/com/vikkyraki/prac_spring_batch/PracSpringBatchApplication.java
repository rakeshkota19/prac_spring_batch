package com.vikkyraki.prac_spring_batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class PracSpringBatchApplication {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

//    @Autowired
//    public JobExecutionDecider deliveryDecider;
//
    @Bean
    public JobExecutionDecider decider() {
        return new DeliveryDecider();
    }

    @Bean
    public JobExecutionDecider paymentDecider() {
        return new PaymentDecider();
    }
    public Step packageItemStep() {
        return this.stepBuilderFactory.get("packageItemStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                // execute will be called until tasklet gets FINISHED;

                // access job parameters by using ChunkContext
                String item = chunkContext.getStepContext().getJobParameters().get("item").toString();
                String date = chunkContext.getStepContext().getJobParameters().get("run.date").toString();

                System.out.println(item + " packaged on " + date);
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step driveToAddressStep() {
        return this.stepBuilderFactory.get("driveToAddressStep").tasklet(new Tasklet() {
            // used to test restart of failed of jobs
            boolean isLost = false;
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Drive to Address step successfully executed");

                if (isLost)
                    throw new RuntimeException("got lost");

                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step givePackageToCustomerStep() {
        return this.stepBuilderFactory.get("givePackageToCustomerStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Package given to customer");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step leavePackageAtDoorStep() {
        return this.stepBuilderFactory.get("leavePackageAtDoorStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Leaving the package at home");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step storePackageStep() {
        return this.stepBuilderFactory.get("givePackageToCustomerStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("package stored, while address cannot be found");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step thankingCustomerStep() {
        return this.stepBuilderFactory.get("thankingCustomerStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Thank you customer for ordering with us");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step initiateRefundStep() {
        return this.stepBuilderFactory.get("initateRefundStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Initiating Refund for the customer");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Job deliverPackageJob() {
        return this.jobBuilderFactory.get("deliverPackageJob").
                                        start(packageItemStep()).
                                        next(driveToAddressStep()).
                                            on("FAILED").to(storePackageStep()).
                                        from(driveToAddressStep()).
                                            on("*").to(decider()).
                                                on("PRESENT").to(givePackageToCustomerStep()).
                                                next(paymentDecider()).
                                                    on("CORRECT").to(thankingCustomerStep()).
                                                from(paymentDecider()).
                                                    on("*").to(initiateRefundStep()).
                                            from(decider()).
                                                on("NOT_PRESENT").to(leavePackageAtDoorStep()).
                                        end().
                                        build();
    }

    public static void main(String[] args) {
        SpringApplication.run(PracSpringBatchApplication.class, args);
    }

}
