package com.pulsaride.dispatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // powers MatchingScheduler's periodic queue polling
public class DispatchCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(DispatchCoreApplication.class, args);
    }
}
