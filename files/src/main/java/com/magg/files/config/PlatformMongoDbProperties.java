package com.magg.files.config;

import javax.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MongoDB configuration properties.
 * Created by miguelgonzalez on 03/09/20
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.mongodb")
public class PlatformMongoDbProperties {

    /**
     * The name of the MongoDB database that should be connected to.
     */
    @NotBlank
    private String database;
    /**
     * MongoDB connection URI.
     */
    @NotBlank
    private String uri;

    /**
     * Getter for {@link #database}.
     *
     * @return {@link #database}
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Setter for {@link #database}.
     *
     * @param database {@link #database}
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * Getter for {@link #uri}.
     *
     * @return {@link #uri}
     */
    public String getUri() {
        return uri;
    }

    /**
     * Setter for {@link #uri}.
     *
     * @param uri {@link #uri}
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
}
