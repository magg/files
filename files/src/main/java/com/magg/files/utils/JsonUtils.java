package com.magg.files.utils;

import com.magg.files.config.ObjectMapperFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;

public class JsonUtils
{

    protected static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getInstance();

    /**
     * Puts or replaces the values of source into target.
     * @param source The source of new values
     * @param target The object where new values are replaced.
     */
    @SuppressWarnings("unchecked")
    public static void mapProperties(Map<String, Object> source, Map<String, Object> target) {
        source.forEach((key, value) -> {
            if (value instanceof Map) {
                //noinspection rawtypes
                mapProperties((Map)source.get(key), (Map)target.get(key));
            } else {
                if (value == Optional.empty()) {
                    target.put(key, null);
                } else {
                    target.put(key, value);
                }

            }
        });
    }

    /**
     * Transforms the object into its json string representation
     * @param object The object to transform
     */
    public static String convertToJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
