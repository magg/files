package com.magg.files.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
public class ExecutorConfig
{
    @Bean
    public ExecutorService fixedThreadPool() {
        return Executors.newFixedThreadPool(50);
    }
}
