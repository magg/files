package com.magg.crypto;

import com.magg.crypto.config.BouncyCastleProviderHolder;
import java.security.Provider;

/**
 * AES encryption/decryption using the Bouncy Castle security provider.
 *
 */
public class AESBouncyEncryption extends SymmetricBlockIVEncryption {


    @Override
    protected String getCipherName() {
        return "AES/GCM/NoPadding";
    }

    @Override
    public Provider getProvider() {
        return BouncyCastleProviderHolder.getInstance();
    }
}
