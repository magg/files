package com.example.file.domain;

import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(AccountDomain.ACCOUNT_COLLECTION_NAME)
public class AccountDomain extends PlatformBaseObject<FileDomain> implements Serializable {

    public static final String ACCOUNT_COLLECTION_NAME = "accounts";
    private static final long serialVersionUID = -4395358318724534245L;

    private String emailAddress;
    private String name;

    public AccountDomain()
    {
    }

    private AccountDomain(Builder builder) {
        this(builder.id, builder.version, builder.createdDate, builder.lastModifiedDate,
            builder.emailAddress, builder.name);
    }


    @PersistenceConstructor
    AccountDomain(
        String id,
        Long version,
        Instant createdDate,
        Instant lastModifiedDate,
        String emailAddress,
        String name)
    {
        this.id = id;
        this.version = version;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.name = name;
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString() {
        return "FileDomain{"
            + "id='" + id + '\''
            + ", version='" + version + '\''
            + ", createdDate='" + createdDate + '\''
            + ", lastModifiedDate='" + lastModifiedDate + '\''
            + ", name='" + name + '\''
            + ", emailAddress='" + emailAddress
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
     * Builder for constructing an {@link AccountDomain}.
     */
    public static class Builder {
        private String id;
        private Long version;
        private Instant createdDate;
        private Instant lastModifiedDate;
        private String name;
        private String emailAddress;

        private Builder() {}

        /**
         * Sets the {@link #name}.
         *
         * @param name {@link #name}
         * @return {@link Builder}
         */
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the {@link #emailAddress}.
         *
         * @param emailAddress {@link #emailAddress}
         * @return {@link Builder}
         */
        public Builder setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
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
         * Creates an {@link AccountDomain}.
         *
         * @return {@link AccountDomain}
         */
        public AccountDomain build() {
            return new AccountDomain(this);
        }
    }

}
