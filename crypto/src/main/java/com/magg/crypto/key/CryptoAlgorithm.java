package com.magg.crypto.key;

import java.util.Arrays;
import java.util.List;

/**
 * List of cryptographic algorithms with available sizes for keys.
 *
 */
public enum CryptoAlgorithm {

    DH(Arrays.asList(1024)),
    DSA(Arrays.asList(1024)),
    RSA(Arrays.asList(2048, 1024, 4096)),
    DES(Arrays.asList(56)),
    AES(Arrays.asList(128));

    private final List<Integer> acceptableSizes;

    CryptoAlgorithm(List<Integer> acceptableSizes) {
        this.acceptableSizes = acceptableSizes;
    }

    /**
     * Get strength of keys generation.
     *
     * @param keyParams Parameters for keys generation
     * @return Size for keys generation
     */
    public int getSize(KeyParams keyParams) {
        int keySize = keyParams.getStrength();
        if (!acceptableSizes.contains(keySize)) {
            throw new IllegalArgumentException("Invalid key size");
        }
        return keySize;
    }

    public int getDefaultSize() {
        return acceptableSizes.get(0);
    }
}
