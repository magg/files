package com.magg.api.exception;

/**
 * StorageException class.
 */
public class StorageException extends RuntimeException {

    /**
     * Constructor for the Storage exception.
     *
     * @param message the message
     * @param cause the message the message
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
