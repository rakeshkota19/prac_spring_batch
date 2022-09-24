package com.vikkyraki.prac_spring_batch.service;

import com.vikkyraki.prac_spring_batch.deciders.DeliveryDecider;
import com.vikkyraki.prac_spring_batch.deciders.PaymentDecider;
import com.vikkyraki.prac_spring_batch.listeners.InitiateRefundStepExecutionListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class DeliveryBatchApplication {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public JobExecutionDecider decider() {
        return new DeliveryDecider();
    }

    @Bean
    public StepExecutionListener refundStepListener() {
        return new InitiateRefundStepExecutionListener();
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
        }).listener(refundStepListener()).build();
    }

    @Bean
    public Step cashRefunderStep() {
        return this.stepBuilderFactory.get("cashRefunderStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Cash will be refunded to the customer");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step amazonOrderInitiatedStep() {
        return this.stepBuilderFactory.get("amazonOrderInitiatedStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Order initiated from the customer end");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    // we can use flow in other jobs
    @Bean
    public Flow deliveryFlow() {
        return new FlowBuilder<SimpleFlow>("deliveryFlow").
                start(driveToAddressStep()).
                on("FAILED").fail().
                from(driveToAddressStep()).
                on("*").to(decider()).
                on("PRESENT").to(givePackageToCustomerStep()).
                next(paymentDecider()).
                on("CORRECT").to(thankingCustomerStep()).
                from(paymentDecider()).
                on("*").to(initiateRefundStep()).
                on("CASH").to(cashRefunderStep()).
                from(decider()).
                on("NOT_PRESENT").to(leavePackageAtDoorStep()).
                build();
    }

    // this will run as seperate job, we can nest in other jobs
    public Step deliveryPackageNestedJobStep() {
        return this.stepBuilderFactory.get("deliveryPackageNestedJobStep").job(deliverPackageJob()).build();
    }

    // using other deliver package flow
//    @Bean
    public Job deliveryAmazonPackageJob() {
        return this.jobBuilderFactory.get("deliveryAmazonPackageJob")
                .start(amazonOrderInitiatedStep())
                .on("*").to(deliveryFlow())
                .end().build();
    }
    //    @Bean
    public Job deliverPackageJob() {
        return this.jobBuilderFactory.get("deliverPackageJob").
                start(packageItemStep()).
//                                        next(driveToAddressStep()).
////                                            on("FAILED").to(storePackageStep()).
////                                              on("FAILED").stop().     // used to stop the job
//                                        on("FAILED").fail().
//                                        from(driveToAddressStep()).
//                                            on("*").to(decider()).
//                                                on("PRESENT").to(givePackageToCustomerStep()).
//                                                next(paymentDecider()).
//                                                    on("CORRECT").to(thankingCustomerStep()).
//                                                from(paymentDecider()).
//                                                    on("*").to(initiateRefundStep()).
//                                                        on("CASH").to(cashRefunderStep()).
//                                            from(decider()).
//                                                on("NOT_PRESENT").to(leavePackageAtDoorStep()).
//                                        end().
        on("*").to(deliveryFlow()).end().build();

        // split(new SimpleAysnTaskExeccuter).add(flow1. flow2)
    }

}
