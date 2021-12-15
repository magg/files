package com.magg.crypto.key;

/**
 * Base interface for operations with size of the cryptographic keys.
 *
 */
public interface CryptoKeySize {

    /**
     * Get key generation strength.
     *
     * @param params Parameters for keys generation
     * @return Strength of the algorithm for keys generation
     */
    int getKeyStrength(KeyParams params);
}
