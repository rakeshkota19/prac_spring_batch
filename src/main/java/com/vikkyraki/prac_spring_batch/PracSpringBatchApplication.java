package com.vikkyraki.prac_spring_batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vikkyraki.prac_spring_batch.service.LinkedinBatchApplication;

@SpringBootApplication
@EnableBatchProcessing
public class PracSpringBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(PracSpringBatchApplication.class, args);
    }
}
