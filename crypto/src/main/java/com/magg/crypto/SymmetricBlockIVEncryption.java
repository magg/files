package com.magg.crypto;

import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Base class for symmetric block cipher encryption/decryption with initialization vector.
 *
 */
public abstract class SymmetricBlockIVEncryption extends BaseEncryption {

    @Override
    protected byte[] doCryptoAction(EncryptionParams params, int mode) throws GeneralSecurityException {
        SymmetricEncryptionIVParams symmetricEncryptionParams = (SymmetricEncryptionIVParams) params;
        SecretKey secretKey = symmetricEncryptionParams.getSecretKey();

        IvParameterSpec ivSpec = new IvParameterSpec(symmetricEncryptionParams.getIv());
        Cipher cipher = getCipher();
        cipher.init(mode, secretKey, ivSpec);
        return cipher.doFinal(symmetricEncryptionParams.getData());

    }

    @Override
    protected void validateParamsType(EncryptionParams params) {
        if (!(params instanceof SymmetricEncryptionIVParams)) {
            throw new IllegalArgumentException("Invalid params type.");
        }
    }
}

