package com.magg.crypto;

import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Base class for encryption and decryption messages with symmetric keys and block cipher.
 *
 */
public abstract class SymmetricBlockEncryption extends BaseEncryption {

    /**
     * Get algorithm.
     *
     * @return Cryptographic algorithm name
     */
    protected abstract String getAlgorithm();

    @Override
    protected byte[] doCryptoAction(EncryptionParams params, int mode) throws GeneralSecurityException {
        SymmetricEncryptionParams symmetricEncryptionParams = (SymmetricEncryptionParams) params;
        SecretKey secretKey = symmetricEncryptionParams.getSecretKey();
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), getAlgorithm());
        Cipher cipher = getCipher();
        cipher.init(mode, keySpec);
        return cipher.doFinal(params.getData());

    }

    @Override
    protected void validateParamsType(EncryptionParams params) {
        if (!(params instanceof SymmetricEncryptionParams)) {
            throw new IllegalArgumentException("Invalid params type.");
        }
    }
}

