package com.magg.crypto.key;

import java.security.Provider;

/**
 * Base class for cryptographic keys generation.
 *
 */
public abstract class KeysBaseGenerator {

    /**
     * Get cryptographic algorithm.
     *
     * @return Cryptographic algorithm name
     */
    public abstract String getAlgorithm();

    /**
     * Get security provider.
     *
     * @return Security provider or null if need using of default security provider
     */
    protected Provider getProvider() {
        return null;
    }
}

