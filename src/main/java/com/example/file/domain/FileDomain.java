package com.example.file.domain;

import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(FileDomain.FILE_COLLECTION_NAME)
public class FileDomain extends PlatformBaseObject<FileDomain> implements Serializable
{

    public static final String FILE_COLLECTION_NAME = "files";
    private static final long serialVersionUID = -5395358318724534245L;

    private String contentType;
    private String filename;
    private String accountId;
    private Long size;
    private String externalId;

    public FileDomain() {
    }

    private FileDomain(Builder builder) {
        this(builder.id, builder.version, builder.createdDate, builder.lastModifiedDate,
            builder.contentType, builder.filename, builder.accountId, builder.size, builder.externalId);
    }

    /**
     * This constructor is needed by Spring {@link MappingMongoConverter} to avoid using reflection which is
     * slower than directly injecting the fields through the constructor.
     */
    @PersistenceConstructor
    FileDomain(
        String id,
        Long version,
        Instant createdDate,
        Instant lastModifiedDate,
        String contentType,
        String filename,
        String accountId,
        Long size,
        String externalId)
    {
        this.id = id;
        this.version = version;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.contentType = contentType;
        this.filename = filename;
        this.accountId = accountId;
        this.size = size;
        this.externalId = externalId;
    }

    public String getContentType()
    {
        return contentType;
    }


    public String getFilename()
    {
        return filename;
    }


    public String getAccountId()
    {
        return accountId;
    }


    public Long getSize()
    {
        return size;
    }


    public String getExternalId()
    {
        return externalId;
    }


    @Override
    public String toString() {
        return "FileDomain{"
            + "id='" + id + '\''
            + ", version='" + version + '\''
            + ", createdDate='" + createdDate + '\''
            + ", lastModifiedDate='" + lastModifiedDate + '\''
            + ", contentType='" + contentType + '\''
            + ", filename='" + filename + '\''
            + ", accountId='" + accountId + '\''
            + ", externalId='" + externalId + '\''
            + ", size=" + size
            + '}';
    }

    /**
     * Creates a new {@link Builder}.
     *
     * @return {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for constructing an {@link FileDomain}.
     */
    public static class Builder {
        private String id;
        private Long version;
        private Instant createdDate;
        private Instant lastModifiedDate;
        private String contentType;
        private String filename;
        private String accountId;
        private Long size;
        private String externalId;

        private Builder() {}

        /**
         * Sets the {@link #contentType}.
         *
         * @param contentType {@link #contentType}
         * @return {@link Builder}
         */
        public Builder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * Sets the {@link #filename}.
         *
         * @param filename {@link #filename}
         * @return {@link Builder}
         */
        public Builder setFilename(String filename) {
            this.filename = filename;
            return this;
        }


        /**
         * Sets the {@link #size}.
         *
         * @param size {@link #size}
         * @return {@link Builder}
         */
        public Builder setSize(Long size) {
            this.size = size;
            return this;
        }


        /**
         * Sets the {@link #accountId}.
         *
         * @param accountId {@link #accountId}
         * @return {@link Builder}
         */
        public Builder setAccountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        /**
         * Sets the {@link #id}.
         *
         * @param id {@link #id}
         * @return {@link Builder}
         */
        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets {@link #createdDate}.
         * @param createdDate {@link #createdDate}
         * @return {@link Builder}
         */
        public Builder setCreatedDate(Instant createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        /**
         * Sets {@link #lastModifiedDate}.
         * @param lastModifiedDate {@link #lastModifiedDate}
         * @return {@link Builder}
         */
        public Builder setLastModifiedDate(Instant lastModifiedDate) {
            this.lastModifiedDate = lastModifiedDate;
            return this;
        }

        /**
         * Sets the {@link #version}.
         *
         * @param version {@link #version}
         * @return {@link Builder}
         */
        public Builder setVersion(Long version) {
            this.version = version;
            return this;
        }

        /**
         * Sets the {@link #externalId}.
         *
         * @param externalId {@link #externalId}
         * @return {@link Builder}
         */
        public Builder setExternalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        /**
         * Creates an {@link FileDomain}.
         *
         * @return {@link FileDomain}
         */
        public FileDomain build() {
            return new FileDomain(this);
        }
    }

}


