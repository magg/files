package com.example.file.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class ObjectMapperFactory
{
    private static final ObjectMapper INSTANCE = initializeMapper();

    private ObjectMapperFactory() {
        // prevent instantiation
    }

    public static ObjectMapper getInstance() {
        return INSTANCE;
    }

    private static ObjectMapper initializeMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
