package com.vikkyraki.prac_spring_batch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;


public class PaymentDecider implements JobExecutionDecider {
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        String exitStatus = new Random().nextFloat() < .70f ? "CORRECT" : "INCORRECT";
        System.out.println("Payment decider exit status is " +  exitStatus);
        return new FlowExecutionStatus(exitStatus);
    }
}
