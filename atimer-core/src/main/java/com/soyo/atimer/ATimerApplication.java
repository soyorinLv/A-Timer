package com.soyo.atimer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.soyo"})
@EnableScheduling
@EnableAsync
public class ATimerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ATimerApplication.class, args);
    }

}
