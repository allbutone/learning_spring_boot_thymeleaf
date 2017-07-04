package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.metrics.repository.InMemoryMetricRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    @Profile("dev")
    CommandLineRunner autoConfigToConsole(ConditionEvaluationReport report) {
        return strings -> report.getConditionAndOutcomesBySource()
                .entrySet().stream()
                .filter(entry -> entry.getValue().isFullMatch())
                .forEach(entry -> System.out.println(entry.getKey() + "==>Match?" + entry.getValue().isFullMatch()));
//        org.springframework.boot.autoconfigure.aop.AopAutoConfiguration==>Match?true
    }

    @Bean
    InMemoryMetricRepository inMemoryMetricRepository(){
        return new InMemoryMetricRepository();
    }
}
