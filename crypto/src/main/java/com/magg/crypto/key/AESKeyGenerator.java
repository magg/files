package com.magg.crypto.key;

/**
 * AES key generation using the default java security provider.
 *
 */
public class AESKeyGenerator extends SymmetricKeyBaseGenerator {

    @Override
    public String getAlgorithm() {
        return CryptoAlgorithm.AES.toString();
    }

    @Override
    public int getKeyStrength(KeyParams params) {
        return CryptoAlgorithm.AES.getSize(params);
    }
}