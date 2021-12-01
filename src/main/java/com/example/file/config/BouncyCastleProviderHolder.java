package com.example.file.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;

/**
 * Thread-safety class for getting Bouncy Castle provider instance.
 *
 */
public final class BouncyCastleProviderHolder {

    private static Provider provider = new BouncyCastleProvider();

    private BouncyCastleProviderHolder() {
    }

    /**
     * Get provider instance.
     *
     * @return Bouncy Castle provider instance
     */
    public static Provider getInstance() {
        return provider;
    }

}