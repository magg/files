package com.magg.crypto.config;

import java.security.Provider;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

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