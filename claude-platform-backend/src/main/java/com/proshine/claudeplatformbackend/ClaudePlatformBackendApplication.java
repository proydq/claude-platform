package com.proshine.claudeplatformbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
public class ClaudePlatformBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaudePlatformBackendApplication.class, args);
    }

}
