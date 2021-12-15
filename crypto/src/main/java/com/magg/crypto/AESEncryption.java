package com.magg.crypto;

import com.magg.crypto.key.CryptoAlgorithm;

/**
 * AES block encryption/decryption using the default java security provider.
 *
 * @author d.zdorovtsev
 */
public class AESEncryption extends SymmetricBlockEncryption {

    @Override
    protected String getCipherName() {
        return "AES/ECB/PKCS5Padding";
    }

    @Override
    protected String getAlgorithm() {
        return CryptoAlgorithm.AES.toString();
    }
}
