package com.magg.crypto.key;

import java.security.spec.KeySpec;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.security.Provider;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Base class for generation symmetric key.
 *
 */
public abstract class SymmetricKeyBaseGenerator extends KeysBaseGenerator
    implements SymmetricKeyGenerator, CryptoKeySize {

    @Override
    public SecretKey generateKey(KeyParams params) throws GeneralSecurityException {
        KeyGenerator keyGenerator = getKeyGenerator();
        int keyStrength = getKeyStrength(params);
        keyGenerator.init(keyStrength);
        return keyGenerator.generateKey();
    }

    public SecretKey readKey(byte[] kb) {
        KeySpec ks = null;
        SecretKey ky = null;
        SecretKeyFactory kf = null;
        ks = new SecretKeySpec(kb,getAlgorithm());
        ky = new SecretKeySpec(kb,getAlgorithm());
        return ky;
    }



    /**
     * Get key generator.
     *
     * @return Key generator
     * @throws GeneralSecurityException if creation of key generator failed
     */
    protected KeyGenerator getKeyGenerator() throws GeneralSecurityException {
        Provider provider = getProvider();
        return provider != null
            ? KeyGenerator.getInstance(getAlgorithm(), provider)
            : KeyGenerator.getInstance(getAlgorithm());
    }
}
