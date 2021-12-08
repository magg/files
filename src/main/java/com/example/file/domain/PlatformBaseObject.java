/*
 * Copyright (c) 2020 Nextiva, Inc. to Present.
 * All rights reserved.
 */

package com.example.file.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

/**
 * The base class for all platform objects. It includes all the necessary interfaces.
 */
public abstract class PlatformBaseObject<T extends PlatformBaseObject> {

    public static final String ID_FIELD = "_id";

    @Id
    @JsonProperty(ID_FIELD)
    protected String id;
    @CreatedDate
    protected Instant createdDate;
    @LastModifiedDate
    protected Instant lastModifiedDate;
    @Version
    protected Long version;

    public String getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

}
