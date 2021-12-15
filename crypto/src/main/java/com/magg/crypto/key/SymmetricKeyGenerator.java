package com.magg.crypto.key;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;

/**
 * Base interface for symmetric key generation.
 *
 */
public interface SymmetricKeyGenerator {

    /**
     * Generate secret key.
     *
     * @param params Parameters for key generation
     * @return Secret key
     * @throws GeneralSecurityException if generation of key failed
     */
    SecretKey generateKey(KeyParams params) throws GeneralSecurityException;
}
