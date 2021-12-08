/*
 * Copyright (c) 2020 Nextiva, Inc. to Present.
 * All rights reserved.
 */

package com.example.file.config;

import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongoV3Driver;
import com.github.cloudyrock.spring.v5.MongockSpring5;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;

import static java.util.Objects.requireNonNull;

/**
 * Spring configurations related to Mongock
 * Created by miguelgonzalez on 03/09/20
 */
@Configuration
@EnableMongoAuditing
public class PlatformMongockConfiguration {
    private static final String CHANGELOG_PACKAGE = "com.example.file";

    private final MongoTemplate mongoTemplate;
    private final ApplicationContext applicationContext;

    /**
     * Constructor.
     *
     * @param mongoTemplate {@link MongoTemplate}
     * @param applicationContext {@link ApplicationContext}
     */
    public PlatformMongockConfiguration(MongoTemplate mongoTemplate, ApplicationContext applicationContext) {
        requireNonNull(mongoTemplate);
        requireNonNull(applicationContext);

        this.mongoTemplate = mongoTemplate;
        this.applicationContext = applicationContext;
    }

    /**
     * Create a {@link MongockSpring5.MongockInitializingBeanRunner} spring bean.
     *
     * @return {@link MongockSpring5.MongockInitializingBeanRunner}
     */
    @Bean
    public MongockSpring5.MongockInitializingBeanRunner mongock() {
        return MongockSpring5.builder()
                .setDriver(SpringDataMongoV3Driver.withDefaultLock(mongoTemplate))
                .addChangeLogsScanPackages(List.of(CHANGELOG_PACKAGE))
                .setSpringContext(applicationContext)
                .buildInitializingBeanRunner();
    }
}
