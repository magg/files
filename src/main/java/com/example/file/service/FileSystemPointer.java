package com.example.file.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileSystemPointer implements FilePointer {

    private final File target;

    public FileSystemPointer(File target) {
        try {
            this.target = target;
            final String contentType = java.nio.file.Files.probeContentType(target.toPath());

        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public InputStream open() {
        try {
            return new BufferedInputStream(new FileInputStream(target));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
