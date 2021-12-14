package com.magg.files.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import static java.util.Objects.requireNonNull;

/**
 * Spring configurations related to MongoDB.
 * Created by miguelgonzalez on 03/09/20
 */
@Configuration
public class PlatformMongoDbConfiguration extends AbstractMongoClientConfiguration {
    private final PlatformMongoDbProperties mongoDbProperties;

    /**
     * Constructor.
     *
     * @param mongoDbProperties {@link PlatformMongoDbProperties}
     */
    public PlatformMongoDbConfiguration(PlatformMongoDbProperties mongoDbProperties) {
        requireNonNull(mongoDbProperties);

        this.mongoDbProperties = mongoDbProperties;
    }

    /**
     * Returns an instance of {@link MongoTransactionManager}.
     *
     * @param dbFactory {@link MongoDatabaseFactory}.
     * @return {@link MongoTransactionManager}.
     */
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("NullableProblems")
    @Bean
    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoDbProperties.getUri());
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public String getDatabaseName() {
        return mongoDbProperties.getDatabase();
    }
}
