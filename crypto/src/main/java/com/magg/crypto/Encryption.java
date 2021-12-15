package com.magg.crypto;

import java.security.GeneralSecurityException;

/**
 * Interface for cryptographic algorithms that are able to encrypt and decrypt messages.
 *
 */
public interface Encryption {

    /**
     * Encrypt data.
     *
     * @param params Parameters for encryption
     * @return Encrypted bytes
     * @throws GeneralSecurityException if encryption failed
     */
    byte[] encrypt(EncryptionParams params) throws GeneralSecurityException;

    /**
     * Decrypt data.
     *
     * @param params Parameters for encryption
     * @return Decrypted bytes
     * @throws GeneralSecurityException if decryption failed
     */
    byte[] decrypt(EncryptionParams params) throws GeneralSecurityException;
}

