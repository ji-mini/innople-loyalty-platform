package com.innople.loyalty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        scanBasePackages = "com.innople.loyalty",
        exclude = UserDetailsServiceAutoConfiguration.class
)
@EnableScheduling
public class LoyaltyPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoyaltyPlatformApplication.class, args);
    }
}

