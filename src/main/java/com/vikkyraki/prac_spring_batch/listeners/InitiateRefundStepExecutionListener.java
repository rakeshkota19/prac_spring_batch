package com.vikkyraki.prac_spring_batch.listeners;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class InitiateRefundStepExecutionListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("Execting before refund initiator step");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("Executing after refund initiator step");

        String paymentType = stepExecution.getJobParameters().getString("paymentType");
        return paymentType.equals("CARD") ? new ExitStatus("CARD") : new ExitStatus("CASH");
    }

}
