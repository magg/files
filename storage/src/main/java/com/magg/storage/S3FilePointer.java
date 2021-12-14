package com.magg.storage;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Optional;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class S3FilePointer implements FilePointer {

    private final ResponseInputStream<GetObjectResponse> target;
    private final String name;
    private final String mediaType;

    public S3FilePointer(ResponseInputStream<GetObjectResponse> target, String name, String mediaType) {
            this.target = target;
            this.name = name;
            this.mediaType = mediaType;
    }

    @Override
    public InputStream open() {
        return new BufferedInputStream(target);
    }


    @Override
    public String getOriginalName()
    {
        return name;
    }


    @Override
    public Optional<String> getMediaType()
    {
        return Optional.ofNullable(mediaType);
    }

}
