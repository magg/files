package com.example.file.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.validation.Valid;

/**
 * Interface for common service implementation.
 */
public interface ObjectConverter<DataT, ApiT>
{

    /**
     * Map {@link Instant} to {@link OffsetDateTime}.
     * @param value {@link Instant}
     * @return {@link OffsetDateTime}
     */
    default OffsetDateTime map(Instant value) {
        return value == null ? null : OffsetDateTime.ofInstant(value, ZoneId.systemDefault());
    }

    /**
     * Map {@link Date} to {@link Instant}.
     * @param value {@link Date}
     * @return {@link Instant}
     */
    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }


    /**
     * Transform public API model to persistence model. Custom fields are ignored for transformation here but
     * should be transformed afterwards.
     * @param object public model.
     * @return a persistence object implementing
     */
    DataT apiModelToDataModel(@Valid ApiT object);

    /**
     * Transform public persistence model to API model. Custom fields are ignored for transformation here but
     * should be transformed afterwards.
     * @param object persistence model.
     * @return a public model.
     */
    ApiT dataModelToApiModel(DataT object);
}
