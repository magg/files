package com.magg.storage;

import java.io.InputStream;
import java.util.Optional;

public interface FilePointer
{

    InputStream open();

    String getOriginalName();

    Optional<String> getMediaType();
}
